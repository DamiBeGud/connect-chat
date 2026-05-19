package com.connectchat.chat.client;

import com.connectchat.chat.client.request.ServiceTokenRequest;
import com.connectchat.chat.client.response.ClientResponse;
import com.connectchat.chat.client.response.ServiceTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServiceTokenClientImpl implements ServiceTokenClient {

    private static final ParameterizedTypeReference<
        ClientResponse<ServiceTokenResponse>
    > SERVICE_TOKEN_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    @Qualifier("identityRestClient")
    private final RestClient identityRestClient;

    public ServiceTokenClientImpl(
        @Qualifier("identityRestClient") RestClient identityRestClient
    ) {
        this.identityRestClient = identityRestClient;
    }

    @Value("${chat.service-client.client-id}")
    private String clientId;

    @Value("${chat.service-client.client-secret}")
    private String clientSecret;

    @Override
    public ServiceTokenResponse issueServiceToken() {
        ClientResponse<ServiceTokenResponse> response = identityRestClient
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
