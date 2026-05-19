package com.connectchat.identity.api;

import com.connectchat.identity.api.request.RefreshTokenRequest;
import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.api.request.ServiceTokenRequest;
import com.connectchat.identity.api.response.AuthTokenResponse;
import com.connectchat.identity.api.response.AuthTokenValidationResponse;
import com.connectchat.identity.api.response.ServiceTokenResponse;
import com.connectchat.identity.common.web.Response;
import com.connectchat.identity.common.web.ResponseFactory;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.ServiceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final AuthService authService;
    private final ServiceTokenService serviceTokenService;
    private final ResponseFactory responseFactory;

    @GetMapping("")
    public ResponseEntity<Void> test() {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Response<Void>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Registration verification code sent",
                null
            )
        );
    }

    @PostMapping("/auth/register/verify")
    public ResponseEntity<Response<AuthTokenResponse>> registerVerification(
        @Valid @RequestBody RegisterVerificationRequest request
    ) {
        AuthTokenResponse tokens = authService.registerVerification(request);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Registration verified",
                tokens
            )
        );
    }

    @PostMapping("/auth/token/refresh")
    public ResponseEntity<Response<AuthTokenResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokenResponse tokens = authService.refreshToken(request);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Token refreshed",
                tokens
            )
        );
    }

    @PostMapping("/auth/service-token")
    public ResponseEntity<Response<ServiceTokenResponse>> serviceToken(
        @Valid @RequestBody ServiceTokenRequest request
    ) {
        ServiceTokenResponse token = serviceTokenService.issueToken(request);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Service token issued",
                token
            )
        );
    }

    @PostMapping("/auth/token/validate")
    public ResponseEntity<Response<AuthTokenValidationResponse>> validateToken(
        @AuthenticationPrincipal Jwt jwt
    ) {
        AuthTokenValidationResponse validation = new AuthTokenValidationResponse(
            jwt.getSubject(),
            jwt.getClaimAsString("token_type"),
            jwt.getClaimAsString("role"),
            jwt.getExpiresAt()
        );

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Token is valid",
                validation
            )
        );
    }
}
