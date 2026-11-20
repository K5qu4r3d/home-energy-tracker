package com.leetjourney.device_service.service.impl;

import com.leetjourney.device_service.dto.DeviceDto;
import com.leetjourney.device_service.entity.Device;
import com.leetjourney.device_service.exception.DeviceNotFoundException;
import com.leetjourney.device_service.mapper.DeviceMapper;
import com.leetjourney.device_service.repository.DeviceRepository;
import com.leetjourney.device_service.service.DeviceService;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository,
                             DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public DeviceDto getDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with ID: " + id));
        return deviceMapper.toDto(device);
    }

    @Override
    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setName(deviceDto.name());
        device.setType(deviceDto.type());
        device.setLocation(deviceDto.location());
        device.setUserId(deviceDto.userId());

        final Device savedDevice = deviceRepository.save(device);
        return deviceMapper.toDto(savedDevice);
    }

    @Override
    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with ID: " + id));

        existing.setName(deviceDto.name());
        existing.setType(deviceDto.type());
        existing.setLocation(deviceDto.location());
        existing.setUserId(deviceDto.userId());

        final Device updatedDevice = deviceRepository.save(existing);
        return deviceMapper.toDto(updatedDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device not found with ID: " + id);
        }

        deviceRepository.deleteById(id);
    }
}
