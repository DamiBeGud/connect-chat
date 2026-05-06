package com.connectchat.identity.service.implementation;

import com.connectchat.identity.common.error.BadRequestException;
import com.connectchat.identity.entity.RefreshToken;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.repository.RefreshTokenRepository;
import com.connectchat.identity.service.RefreshTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${identity.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        Instant now = Instant.now();
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(hashToken(rawToken))
            .expiresAt(now.plusMillis(refreshTokenExpirationMs))
            .revoked(false)
            .createdAt(now)
            .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        Instant now = Instant.now();
        RefreshToken storedToken = refreshTokenRepository
            .findByTokenHashForUpdate(hashToken(rawToken))
            .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            revokeAllActiveTokens(storedToken.getUserId());
            throw new BadRequestException("Refresh token reuse detected");
        }

        if (storedToken.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("Refresh token expired");
        }

        return storedToken;
    }

    @Override
    @Transactional
    public void revoke(RefreshToken token) {
        token.revoke();
        refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void revokeAllActiveTokens(UUID userId) {
        List<RefreshToken> activeTokens =
            refreshTokenRepository.findAllByUserIdAndRevokedFalseAndExpiresAtAfter(
                userId,
                Instant.now()
            );

        if (activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeTokens);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
