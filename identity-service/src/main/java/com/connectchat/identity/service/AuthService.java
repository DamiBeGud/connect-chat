package com.connectchat.identity.service;

import com.connectchat.identity.api.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
}
