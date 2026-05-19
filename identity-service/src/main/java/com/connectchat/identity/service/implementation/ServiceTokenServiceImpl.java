package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.ServiceTokenRequest;
import com.connectchat.identity.api.response.ServiceTokenResponse;
import com.connectchat.identity.common.error.ForbiddenException;
import com.connectchat.identity.config.InternalServiceClientProperties;
import com.connectchat.identity.config.InternalServiceClientProperties.InternalServiceClient;
import com.connectchat.identity.service.JwtService;
import com.connectchat.identity.service.ServiceTokenService;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceTokenServiceImpl implements ServiceTokenService {

    private static final String SERVICE_TOKEN_TYPE = "service";
    private static final String INTERNAL_SERVICE_ROLE = "INTERNAL_SERVICE";

    private final JwtService jwtService;
    private final InternalServiceClientProperties clientProperties;
    private final long serviceTokenExpirationMs;

    public ServiceTokenServiceImpl(
        JwtService jwtService,
        InternalServiceClientProperties clientProperties,
        @Value("${identity.jwt.service-token-expiration-ms}") long serviceTokenExpirationMs
    ) {
        this.jwtService = jwtService;
        this.clientProperties = clientProperties;
        this.serviceTokenExpirationMs = serviceTokenExpirationMs;
    }

    @Override
    public ServiceTokenResponse issueToken(ServiceTokenRequest request) {
        InternalServiceClient client = clientProperties
            .findByClientId(request.clientId())
            .filter(configuredClient ->
                Objects.equals(
                    configuredClient.clientSecret(),
                    request.clientSecret()
                )
            )
            .orElseThrow(() ->
                new ForbiddenException("Invalid internal service credentials")
            );

        return new ServiceTokenResponse(
            jwtService.generateServiceToken(client.clientId()),
            SERVICE_TOKEN_TYPE,
            INTERNAL_SERVICE_ROLE,
            Instant.now().plusMillis(serviceTokenExpirationMs)
        );
    }
}
