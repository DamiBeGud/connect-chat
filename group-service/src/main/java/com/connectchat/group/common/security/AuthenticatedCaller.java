package com.connectchat.group.common.security;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthenticatedCaller implements Authentication {

    private final String subject;
    private final String tokenType;
    private final String role;
    private final Instant expiresAt;
    private final List<GrantedAuthority> authorities;
    private boolean authenticated = true;

    public AuthenticatedCaller(
        String subject,
        String tokenType,
        String role,
        Instant expiresAt
    ) {
        this.subject = subject;
        this.tokenType = tokenType;
        this.role = role;
        this.expiresAt = expiresAt;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public UUID requireUserId() {
        return UUID.fromString(subject);
    }

    public boolean isUserToken() {
        return "user".equals(tokenType);
    }

    public boolean isServiceToken() {
        return "service".equals(tokenType);
    }

    public String subject() {
        return subject;
    }

    public String tokenType() {
        return tokenType;
    }

    public String role() {
        return role;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return subject;
    }
}
