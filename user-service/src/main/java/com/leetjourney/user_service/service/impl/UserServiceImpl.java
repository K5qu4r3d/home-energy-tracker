package com.leetjourney.user_service.service.impl;

import com.leetjourney.user_service.dto.UserDto;
import com.leetjourney.user_service.entity.User;
import com.leetjourney.user_service.exception.UserNotFoundException;
import com.leetjourney.user_service.mapper.UserMapper;
import com.leetjourney.user_service.repository.UserRepository;
import com.leetjourney.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        final User createdUser = User.builder()
                .name(userDto.name())
                .surname(userDto.surname())
                .email(userDto.email())
                .address(userDto.address())
                .alerting(userDto.alerting())
                .energyAlertingThreshold(userDto.energyAlertingThreshold())
                .build();

        final User saved = userRepository.save(createdUser);
        return userMapper.toDto(saved);
    }

    @Override
    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
    }

    @Override
    public void updateUser(Long id, UserDto userDto) {
        // Retrieve user from repository
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update with new information
        user.setName(userDto.name());
        user.setSurname(userDto.surname());
        user.setEmail(userDto.email());
        user.setAddress(userDto.address());
        user.setAlerting(userDto.alerting());
        user.setEnergyAlertingThreshold(userDto.energyAlertingThreshold());

        // Save to repository
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }
}
