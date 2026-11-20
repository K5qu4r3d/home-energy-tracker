package com.leetjourney.device_service.service;

import com.leetjourney.device_service.dto.DeviceDto;

public interface DeviceService {
    DeviceDto getDevice(Long id);
    DeviceDto createDevice(DeviceDto deviceDto);
    DeviceDto updateDevice(Long id, DeviceDto deviceDto);
    void deleteDevice(Long id);
}
