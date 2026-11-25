package com.leetjourney.usage_service.client;

import com.leetjourney.usage_service.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserClient {
//    private final RestTemplate restTemplate;
    private final RestClient restClient;
    private final String baseUrl;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
//        this.restTemplate = new RestTemplate();
        this.restClient = RestClient.create();
        this.baseUrl = baseUrl;
    }

    public UserDto getUser(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .buildAndExpand(userId).toUriString();

//        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        ResponseEntity<UserDto> response = restClient.get().uri(url).retrieve().toEntity(UserDto.class);
        return response.getBody();
    }
}
