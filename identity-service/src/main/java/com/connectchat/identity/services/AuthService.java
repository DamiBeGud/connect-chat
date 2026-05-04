package com.connectchat.identity.services;

import com.connectchat.identity.dto.RegisterRequest;
import com.connectchat.identity.util.Response;

public interface AuthService {
    Response<Void> register(RegisterRequest request);
}
