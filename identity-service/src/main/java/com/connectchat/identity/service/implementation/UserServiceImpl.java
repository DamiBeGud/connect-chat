package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.common.error.BadRequestException;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.repository.UserRepository;
import com.connectchat.identity.service.UserService;
import lombok.RequiredArgsConstructor;
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

        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void markValidationCodeSent(User user) {
        user.markValidationCodeSent();
        userRepository.save(user);
    }
}
