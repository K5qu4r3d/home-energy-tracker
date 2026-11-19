package com.leetjourney.userservice.mapper;

import com.leetjourney.userservice.dto.UserDto;
import com.leetjourney.userservice.entity.User;

public interface UserMapper {
    UserDto toDto(User user);
//    User toEntity(UserDto userDto);
}
