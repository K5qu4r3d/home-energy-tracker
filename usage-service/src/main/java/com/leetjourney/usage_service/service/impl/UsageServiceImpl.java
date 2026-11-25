package com.leetjourney.usage_service.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import com.leetjourney.kafka.event.AlertingEvent;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import com.leetjourney.usage_service.client.DeviceClient;
import com.leetjourney.usage_service.client.UserClient;
import com.leetjourney.usage_service.dto.DeviceDto;
import com.leetjourney.usage_service.dto.UserDto;
import com.leetjourney.usage_service.model.DeviceEnergy;
import com.leetjourney.usage_service.service.UsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageServiceImpl implements UsageService {
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    private final InfluxDBClient influxDBClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    public UsageServiceImpl(InfluxDBClient influxDBClient,
                            DeviceClient deviceClient,
                            UserClient userClient,
                            KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent event) {
//        log.info("Received energy usage event: {}", event);

        // Create a Point to send to Influx
        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(event.deviceId()))
                .addField("energyConsumed", event.energyConsumed())
                .time(event.timestamp(), WritePrecision.MS);

        // Push the data to Influx
        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    private void sendToKafka(Long userId,
                             String message,
                             Double threshold,
                             Double totalConsumption,
                             String email) {
        // Put message on Kafka alert-topic
        final AlertingEvent alertingEvent = AlertingEvent.builder()
                .userId(userId)
                .message(message)
                .threshold(threshold)
                .energyConsumed(totalConsumption)
                .email(email)
                .build();

        // Send alerting event to Kafka topic
        kafkaTemplate.send("energy-alerts", alertingEvent);
    }

    private List<FluxTable> queryForTables() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
                from(bucket: "%s")
                    |> range(start: time(v: "%s"), stop: time(v: "%s"))
                    |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                    |> filter(fn: (r) => r["_field"] == "energyConsumed")
                    |> group(columns: ["deviceId"])
                    |> sum(column: "_value")
                """, influxBucket, oneHourAgo.toString(), now);

        // Send query to Influx
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, influxOrg);
    }

    private List<DeviceEnergy> consolidateData(List<FluxTable> tables) {
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            table.getRecords().forEach(record -> {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number ?
                        ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                deviceEnergies.add(DeviceEnergy.builder()
                        .deviceId(Long.valueOf(deviceIdStr))
                        .energyConsumed(energyConsumed)
                        .build());
            });
        }

        log.info("Aggregate device energies over the past hour: {}", deviceEnergies);

        return deviceEnergies;
    }

    private Map<Long, List<DeviceEnergy>> mapUserToDevice(List<DeviceEnergy> deviceEnergies) {
        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                final DeviceDto deviceResponse = deviceClient.getDevice(deviceEnergy.getDeviceId());

                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("Device not found for ID: {}", deviceEnergy.getDeviceId());
                    continue;
                }

                // Get the device currently being considered and update its ID
                deviceEnergy.setUserId(deviceResponse.id());
            } catch (Exception e) {
                log.warn("Failed to fetch device for ID: {}", deviceEnergy.getDeviceId());
            }
        }

        // Remove any devices with null userId
        deviceEnergies.removeIf(de -> de.getUserId() == null);

        // Get device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream()
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device energy map: {}", userDeviceEnergyMap);

        return userDeviceEnergyMap;
    }

    private void mapUserToThreshold(List<Long> userIds,
                                    Map<Long, Double> userThresholdMap,
                                    Map<Long, String> userEmailMap) {
        for (final Long userId : userIds) {
            try {
                UserDto user = userClient.getUser(userId);

                // Verify using null checks
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User not found or alerting disabled for ID: {}", userId);
                    continue;
                }

                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.warn("Failed to fetch user for ID: {}", userId);
            }
        }

        log.info("User threshold map: {}", userThresholdMap);
    }

    private void checkThresholds(Map<Long, List<DeviceEnergy>> userDeviceEnergyMap,
                                 Map<Long, Double> userThresholdMap,
                                 Map<Long, String> userEmailMap) {
        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());

        for (final Long userId : alertedUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();

            if (totalConsumption > threshold) {
                String message = "Energy consumption threshold exceeded";
                String email = userEmailMap.get(userId);
                sendToKafka(userId, message, threshold, totalConsumption, email);
            } else {
                log.info("ALERT: User ID {} is within the energy threshold. " +
                                "Total consumption: {}, Threshold: {}",
                        userId, totalConsumption, threshold);
            }
        }
    }

    /**
     * Aggregate device data based on ID and send alert once energy threshold is crossed
     */
    @Override
    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        List<FluxTable> tables = queryForTables();

        // Consolidate incoming data into our own model
        List<DeviceEnergy> deviceEnergies = consolidateData(tables);

        // Connect each device to the user it belongs to
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = mapUserToDevice(deviceEnergies);

        // Get users' energy consumption thresholds
        //     and check whether the user has activated alerting
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        mapUserToThreshold(userIds, userThresholdMap, userEmailMap);

        // Check thresholds against aggregated usage
        checkThresholds(userDeviceEnergyMap, userThresholdMap, userEmailMap);
    }
}
