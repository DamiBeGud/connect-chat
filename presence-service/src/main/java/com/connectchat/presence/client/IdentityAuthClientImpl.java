package com.connectchat.presence.client;

import com.connectchat.presence.common.web.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class IdentityAuthClientImpl implements IdentityAuthClient {

    private static final ParameterizedTypeReference<
        Response<IdentityTokenValidationResponse>
    > VALIDATION_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient identityRestClient;

    @Override
    public IdentityTokenValidationResponse validateToken(String bearerToken) {
        Response<IdentityTokenValidationResponse> response = identityRestClient
            .post()
            .uri("/api/v1/identity/auth/token/validate")
            .header("Authorization", bearerToken)
            .retrieve()
            .body(VALIDATION_RESPONSE_TYPE);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Identity service returned no token data");
        }

        return response.data();
    }
}
