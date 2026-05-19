package com.connectchat.chat.client;

import com.connectchat.chat.client.response.ClientResponse;
import com.connectchat.chat.client.response.IdentityTokenValidationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityAuthClientImpl implements IdentityAuthClient {

    private static final ParameterizedTypeReference<
        ClientResponse<IdentityTokenValidationResponse>
    > VALIDATION_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient identityRestClient;

    public IdentityAuthClientImpl(
        @Qualifier("identityRestClient") RestClient identityRestClient
    ) {
        this.identityRestClient = identityRestClient;
    }

    @Override
    public IdentityTokenValidationResponse validateToken(
        String authorizationHeader
    ) {
        ClientResponse<IdentityTokenValidationResponse> response =
            identityRestClient
                .post()
                .uri("/api/v1/identity/auth/token/validate")
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(VALIDATION_RESPONSE_TYPE);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Identity service returned no token data");
        }

        return response.data();
    }
}
