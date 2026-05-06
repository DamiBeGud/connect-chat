package com.connectchat.identity.service.implementation;

import com.connectchat.identity.entity.User;
import com.connectchat.identity.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String USER_ROLE = "USER";
    private static final String SERVICE_ROLE = "INTERNAL_SERVICE";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long serviceTokenExpirationMs;
    private final String issuer;

    public JwtServiceImpl(
        @Value("${identity.jwt.secret}") String secret,
        @Value("${identity.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
        @Value("${identity.jwt.service-token-expiration-ms}") long serviceTokenExpirationMs,
        @Value("${identity.jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.serviceTokenExpirationMs = serviceTokenExpirationMs;
        this.issuer = issuer;
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(user.getId().toString())
            .issuer(issuer)
            .claim("token_type", "user")
            .claim("role", USER_ROLE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    @Override
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @Override
    public String generateServiceToken(String clientId) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(clientId)
            .issuer(issuer)
            .claim("token_type", "service")
            .claim("role", SERVICE_ROLE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(serviceTokenExpirationMs)))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }
}
