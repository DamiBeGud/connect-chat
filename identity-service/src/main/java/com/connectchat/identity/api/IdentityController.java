package com.connectchat.identity.api;

import com.connectchat.identity.api.request.RefreshTokenRequest;
import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.request.RegisterVerificationRequest;
import com.connectchat.identity.api.request.ServiceTokenRequest;
import com.connectchat.identity.api.response.AuthTokenResponse;
import com.connectchat.identity.api.response.AuthTokenValidationResponse;
import com.connectchat.identity.api.response.IdentityUserResponse;
import com.connectchat.identity.api.response.ServiceTokenResponse;
import com.connectchat.identity.common.error.ForbiddenException;
import com.connectchat.identity.common.web.Response;
import com.connectchat.identity.common.web.ResponseFactory;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.ServiceTokenService;
import com.connectchat.identity.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    private static final String SERVICE_TOKEN_TYPE = "service";
    private static final String SERVICE_ROLE = "INTERNAL_SERVICE";

    private final AuthService authService;
    private final ServiceTokenService serviceTokenService;
    private final UserService userService;
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

    @GetMapping("/users/{userId}")
    public ResponseEntity<Response<IdentityUserResponse>> getUserById(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID userId
    ) {
        requireInternalService(jwt);
        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "User fetched",
                toResponse(user)
            )
        );
    }

    @GetMapping("/users/by-phone/{phoneNumber}")
    public ResponseEntity<Response<IdentityUserResponse>> getUserByPhoneNumber(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String phoneNumber
    ) {
        requireInternalService(jwt);
        User user = userService.getUserByPhoneNumber(phoneNumber);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "User fetched",
                toResponse(user)
            )
        );
    }

    private IdentityUserResponse toResponse(User user) {
        return new IdentityUserResponse(user.getId(), user.getPhoneNumber());
    }

    private void requireInternalService(Jwt jwt) {
        if (
            jwt == null ||
            !SERVICE_TOKEN_TYPE.equals(jwt.getClaimAsString("token_type")) ||
            !SERVICE_ROLE.equals(jwt.getClaimAsString("role"))
        ) {
            throw new ForbiddenException("Internal service token is required");
        }
    }
}
