package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.common.error.BadRequestException;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.repository.UserRepository;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.SmsClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
    implements AuthService
{
    private static final Random RANDOM = new Random();

    private final SmsClient smsClient;
    private final UserRepository userRepository;

    @Override
    public void register(RegisterRequest request) {
        User user = createUser(request);
        smsClient.sendVerificationCode(
            user.getPhoneNumber(),
            user.getVerificationCode()
        );
    }

    private User createUser(RegisterRequest request) {
        if (
            userRepository.existsByPhoneNumber(request.phoneNumber())
        ) throw new BadRequestException("");

        User newUser = User.builder()
            .phoneNumber(request.phoneNumber())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .nickname(request.nickname())
            .dateOfBirth(request.dateOfBirth())
            .country(request.country())
            .verificationCode(generateValidationCode())
            .verificationCodeExpiresAt(
                Instant.now().plus(5, ChronoUnit.MINUTES)
            )
            .build();

        return userRepository.save(newUser);
    }

    private String generateValidationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
