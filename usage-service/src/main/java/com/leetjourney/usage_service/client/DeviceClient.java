package com.leetjourney.usage_service.client;

import com.leetjourney.usage_service.dto.DeviceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
//import org.springframework.web.client.RestTemplate;

@Component
public class DeviceClient {
//    private final RestTemplate restTemplate;
    private final RestClient restClient;
    private final String baseUrl;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
//        this.restTemplate = new RestTemplate();
        this.restClient = RestClient.create();
        this.baseUrl = baseUrl;
    }

    public DeviceDto getDevice(Long deviceId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(deviceId).toUriString();

//        ResponseEntity<DeviceDto> response = restTemplate.getForEntity(url, DeviceDto.class);
        ResponseEntity<DeviceDto> response = restClient.get().uri(url).retrieve().toEntity(DeviceDto.class);
        return response.getBody();
    }
}
