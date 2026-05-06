package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.api.response.AuthTokenResponse;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.JwtService;
import com.connectchat.identity.service.RefreshTokenService;
import com.connectchat.identity.service.SmsClient;
import com.connectchat.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SmsClient smsClient;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void register(RegisterRequest request) {
        User user = userService.createUser(request);
        smsClient.sendVerificationCode(
            user.getPhoneNumber(),
            user.getVerificationCode()
        );
        userService.markValidationCodeSent(user);
    }

    @Override
    public AuthTokenResponse registerVerification(
        RegisterVerificationRequest request
    ) {
        User user = userService.verifyRegistration(request);

        return new AuthTokenResponse(
            jwtService.generateAccessToken(user),
            refreshTokenService.createRefreshToken(user)
        );
    }
}
