package com.leetjourney.user_service.service;

import com.leetjourney.user_service.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(Long id);
    void updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
}
