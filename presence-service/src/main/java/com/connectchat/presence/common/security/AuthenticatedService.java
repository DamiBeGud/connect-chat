package com.connectchat.presence.common.security;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthenticatedService implements Authentication {

    private final String subject;
    private final String tokenType;
    private final String role;
    private final Instant expiresAt;
    private final List<GrantedAuthority> authorities;
    private boolean authenticated = true;

    public AuthenticatedService(
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

    public boolean isInternalService() {
        return "service".equals(tokenType) &&
        "INTERNAL_SERVICE".equals(role);
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
