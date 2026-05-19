package com.connectchat.group.config;

import com.connectchat.group.client.IdentityAuthClient;
import com.connectchat.group.client.IdentityTokenValidationResponse;
import com.connectchat.group.common.security.AuthenticatedCaller;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class IdentityAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final IdentityAuthClient identityAuthClient;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (
            authorizationHeader == null ||
            !authorizationHeader.startsWith(BEARER_PREFIX)
        ) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            IdentityTokenValidationResponse validation =
                identityAuthClient.validateToken(authorizationHeader);
            SecurityContextHolder.getContext()
                .setAuthentication(
                    new AuthenticatedCaller(
                        validation.subject(),
                        validation.tokenType(),
                        validation.role(),
                        validation.expiresAt()
                    )
                );
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
