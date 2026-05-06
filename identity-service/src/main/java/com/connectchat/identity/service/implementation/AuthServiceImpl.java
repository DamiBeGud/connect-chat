package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.SmsClient;
import com.connectchat.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SmsClient smsClient;
    private final UserService userService;

    @Override
    public void register(RegisterRequest request) {
        User user = userService.createUser(request);
        smsClient.sendVerificationCode(
            user.getPhoneNumber(),
            user.getVerificationCode()
        );
        userService.markValidationCodeSent(user);
    }
}
