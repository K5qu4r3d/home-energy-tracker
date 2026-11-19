package com.leetjourney.userservice.service;

import com.leetjourney.userservice.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(Long id);
    void updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
}
