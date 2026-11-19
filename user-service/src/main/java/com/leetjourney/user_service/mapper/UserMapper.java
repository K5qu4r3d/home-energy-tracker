package com.leetjourney.user_service.mapper;

import com.leetjourney.user_service.dto.UserDto;
import com.leetjourney.user_service.entity.User;

public interface UserMapper {
    UserDto toDto(User user);
//    User toEntity(UserDto userDto);
}
