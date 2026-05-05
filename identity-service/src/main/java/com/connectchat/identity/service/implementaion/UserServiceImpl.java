package com.connectchat.identity.service.implementaion;

import com.connectchat.identity.dto.UserDto;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.exception.BadRequestException;
import com.connectchat.identity.repository.UserRepository;
import com.connectchat.identity.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Random random;
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
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
