package com.leetjourney.device_service.mapper;

import com.leetjourney.device_service.dto.DeviceDto;
import com.leetjourney.device_service.entity.Device;

public interface DeviceMapper {
    DeviceDto toDto(Device device);
}
