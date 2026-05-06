package com.connectchat.identity.service;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.api.response.AuthTokenResponse;

public interface AuthService {
    void register(RegisterRequest request);

    AuthTokenResponse registerVerification(RegisterVerificationRequest request);
}
