package com.leetjourney.device_service.mapper.impl;

import com.leetjourney.device_service.dto.DeviceDto;
import com.leetjourney.device_service.entity.Device;
import com.leetjourney.device_service.mapper.DeviceMapper;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapperImpl implements DeviceMapper {
    @Override
    public DeviceDto toDto(Device device) {
        return new DeviceDto(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getLocation(),
                device.getUserId()
        );
    }
}
