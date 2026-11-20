package com.leetjourney.ingestion_service.service.impl;

import com.leetjourney.ingestion_service.dto.EnergyUsageDto;
import com.leetjourney.ingestion_service.service.IngestionService;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IngestionServiceImpl implements IngestionService {
    private final KafkaTemplate<String, EnergyUsageEvent>  kafkaTemplate;

    public IngestionServiceImpl(KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void ingestEnergyUsage(EnergyUsageDto usageDto) {
        // Convert DTO to Event
        EnergyUsageEvent event = EnergyUsageEvent.builder()
                .deviceId(usageDto.deviceId())
                .energyConsumed(usageDto.energyConsumed())
                .timestamp(usageDto.timestamp())
                .build();

        // Send to Kafka topic
        kafkaTemplate.send("energy-usage", event);
        log.info("Ingested Energy Usage Event: {}", event);
    }
}
