package com.leetjourney.ingestion_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record EnergyUsageDto(
        Long deviceId,
        Double energyConsumes,

        @JsonFormat(shape =  JsonFormat.Shape.STRING)
        Instant timestamp
) {}