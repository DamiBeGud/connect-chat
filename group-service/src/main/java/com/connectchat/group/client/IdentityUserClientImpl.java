package com.connectchat.group.client;

import com.connectchat.group.common.web.Response;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityUserClientImpl implements IdentityUserClient {

    private static final ParameterizedTypeReference<
        Response<IdentityUserResponse>
    > USER_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient identityRestClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public IdentityUserClientImpl(
        @Qualifier("identityRestClient") RestClient identityRestClient,
        ServiceTokenProvider serviceTokenProvider
    ) {
        this.identityRestClient = identityRestClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Override
    public IdentityUserResponse getUserById(UUID userId) {
        Response<IdentityUserResponse> response = identityRestClient
            .get()
            .uri("/api/v1/identity/users/{userId}", userId)
            .header("Authorization", authorizationHeader())
            .retrieve()
            .body(USER_RESPONSE_TYPE);

        return requireData(response);
    }

    @Override
    public IdentityUserResponse getUserByPhoneNumber(String phoneNumber) {
        Response<IdentityUserResponse> response = identityRestClient
            .get()
            .uri("/api/v1/identity/users/by-phone/{phoneNumber}", phoneNumber)
            .header("Authorization", authorizationHeader())
            .retrieve()
            .body(USER_RESPONSE_TYPE);

        return requireData(response);
    }

    private String authorizationHeader() {
        return serviceTokenProvider.authorizationHeader();
    }

    private <T> T requireData(Response<T> response) {
        if (response == null || response.data() == null) {
            throw new IllegalStateException("Identity service returned no user data");
        }

        return response.data();
    }
}
