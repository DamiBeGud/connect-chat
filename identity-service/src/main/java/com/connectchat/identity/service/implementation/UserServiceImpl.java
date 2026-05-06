package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.common.error.BadRequestException;
import com.connectchat.identity.common.error.ResourceNotFoundException;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.repository.UserRepository;
import com.connectchat.identity.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }

        User newUser = User.builder()
            .phoneNumber(request.phoneNumber())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .nickname(request.nickname())
            .dateOfBirth(request.dateOfBirth())
            .country(request.country())
            .build();

        try {
            return userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException exception) {
            throw new BadRequestException("Phone number is already registered");
        }
    }

    @Override
    @Transactional
    public void markValidationCodeSent(User user) {
        user.markValidationCodeSent();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User verifyRegistration(RegisterVerificationRequest request) {
        User user = userRepository
            .findByPhoneNumber(request.phoneNumber())
            .orElseThrow(() -> new BadRequestException("Invalid verification code"));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified");
        }

        if (!user.isValidationCodeSent()) {
            throw new BadRequestException("Verification code was not sent");
        }

        if (
            user.getVerificationCodeExpiresAt() == null ||
            user.getVerificationCodeExpiresAt().isBefore(Instant.now())
        ) {
            throw new BadRequestException("Verification code expired");
        }

        if (!request.verificationCode().equals(user.getVerificationCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        user.markVerified();
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
