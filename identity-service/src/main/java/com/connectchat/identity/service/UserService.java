package com.connectchat.identity.service;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.entity.User;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);

    void markValidationCodeSent(User user);

    User verifyRegistration(RegisterVerificationRequest request);

    User getUserById(UUID userId);
}
