package com.leetjourney.usage_service.service;

import com.leetjourney.kafka.event.EnergyUsageEvent;

public interface UsageService {
    void energyUsageEvent(EnergyUsageEvent event);
}
