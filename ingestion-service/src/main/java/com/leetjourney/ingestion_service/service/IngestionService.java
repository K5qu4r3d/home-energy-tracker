package com.leetjourney.ingestion_service.service;

import com.leetjourney.ingestion_service.dto.EnergyUsageDto;

public interface IngestionService {
    void ingestEnergyUsage(EnergyUsageDto usageDto);
}
