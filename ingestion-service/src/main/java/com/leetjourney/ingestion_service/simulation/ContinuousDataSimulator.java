package com.leetjourney.ingestion_service.simulation;

import com.leetjourney.ingestion_service.dto.EnergyUsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

@Slf4j
@Component
public class ContinuousDataSimulator implements CommandLineRunner {
    private final RestClient restClient = RestClient.create();
    private final Random random = new Random();

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("ContinuousDataSimulator started...");
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        for (int i = 0; i < requestsPerInterval; i++) {
            EnergyUsageDto dto = EnergyUsageDto.builder()
                    .deviceId(random.nextLong(1, 6))
                    .energyConsumed(Math.round(random.nextDouble(0.0, 10.0) * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                    .build();

            try {
                restClient.post()
                        .uri(ingestionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(dto)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Sent mock data: {}", dto);
            } catch (Exception e) {
                log.error("Failed to send data: {}", e.getMessage());
            }
        }
    }
}
