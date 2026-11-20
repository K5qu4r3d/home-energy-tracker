package com.leetjourney.ingestion_service.service.impl;

import com.leetjourney.ingestion_service.dto.EnergyUsageDto;
import com.leetjourney.ingestion_service.service.IngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IngestionServiceImpl implements IngestionService {
    @Override
    public void ingestEnergyUsage(EnergyUsageDto usageDto) {

    }
}
