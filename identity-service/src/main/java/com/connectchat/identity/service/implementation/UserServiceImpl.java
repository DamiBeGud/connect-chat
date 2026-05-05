package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.response.UserDto;
import com.connectchat.identity.common.error.BadRequestException;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.repository.UserRepository;
import com.connectchat.identity.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private static final Random RANDOM = new Random();

    private final UserRepository userRepository;

    @Override
    public User createUser(UserDto userDto) {
        if (
            userRepository.existsByPhoneNumber(userDto.phoneNumber())
        ) throw new BadRequestException("");
        User newUser = User.builder()
            .phoneNumber(userDto.phoneNumber())
            .firstName(userDto.firstName())
            .lastName(userDto.lastName())
            .nickname(userDto.nickname())
            .dateOfBirth(userDto.dateOfBirth())
            .country(userDto.country())
            .verificationCode(generateValidationCode())
            .verificationCodeExpiresAt(
                Instant.now().plus(5, ChronoUnit.MINUTES)
            )
            .build();

        User user = userRepository.save(newUser);
        return user;
    }

    private String generateValidationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
