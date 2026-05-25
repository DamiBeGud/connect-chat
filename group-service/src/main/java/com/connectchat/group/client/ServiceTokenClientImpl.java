package com.connectchat.group.client;

import com.connectchat.group.common.web.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServiceTokenClientImpl implements ServiceTokenClient {

    private static final ParameterizedTypeReference<
        Response<ServiceTokenResponse>
    > SERVICE_TOKEN_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient identityRestClient;
    private final String clientId;
    private final String clientSecret;

    public ServiceTokenClientImpl(
        @Qualifier("identityRestClient") RestClient identityRestClient,
        @Value("${group.service-client.client-id}") String clientId,
        @Value("${group.service-client.client-secret}") String clientSecret
    ) {
        this.identityRestClient = identityRestClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public ServiceTokenResponse issueServiceToken() {
        Response<ServiceTokenResponse> response = identityRestClient
            .post()
            .uri("/api/v1/identity/auth/service-token")
            .body(new ServiceTokenRequest(clientId, clientSecret))
            .retrieve()
            .body(SERVICE_TOKEN_RESPONSE_TYPE);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Identity service returned no service token");
        }

        return response.data();
    }
}
