package com.leetjourney.usage_service.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import com.leetjourney.usage_service.service.UsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsageServiceImpl implements UsageService {
    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    private final InfluxDBClient influxDBClient;

    public UsageServiceImpl(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    @Override
    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent event) {
        log.info("Received energy usage event: {}", event);

        // Create a Point to send to Influx
        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(event.deviceId()))
                .addField("energyConsumed", event.energyConsumed())
                .time(event.timestamp(), WritePrecision.MS);

        // Push the data to Influx
        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }
}
