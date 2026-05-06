package com.connectchat.identity.service;

import com.connectchat.identity.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateAccessToken(User user);

    Claims parseAccessToken(String token);

    String generateServiceToken(String clientId);
}
