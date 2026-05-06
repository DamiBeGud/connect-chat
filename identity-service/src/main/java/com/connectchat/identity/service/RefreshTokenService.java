package com.connectchat.identity.service;

import com.connectchat.identity.entity.RefreshToken;
import com.connectchat.identity.entity.User;
import java.util.UUID;

public interface RefreshTokenService {
    String createRefreshToken(User user);

    RefreshToken validateRefreshToken(String rawToken);

    void revoke(RefreshToken token);

    void revokeAllActiveTokens(UUID userId);
}
