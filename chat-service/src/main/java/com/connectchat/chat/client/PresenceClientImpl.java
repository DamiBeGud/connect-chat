package com.connectchat.chat.client;

import com.connectchat.chat.client.request.PresenceLookupRequest;
import com.connectchat.chat.client.request.RegisterPresenceSessionRequest;
import com.connectchat.chat.client.response.ClientResponse;
import com.connectchat.chat.client.response.PresenceResponse;
import com.connectchat.chat.client.response.UserOnlineResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PresenceClientImpl implements PresenceClient {

    private static final ParameterizedTypeReference<
        ClientResponse<PresenceResponse>
    > PRESENCE_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<
        ClientResponse<List<PresenceResponse>>
    > PRESENCE_LIST_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<
        ClientResponse<UserOnlineResponse>
    > USER_ONLINE_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    @Qualifier("presenceRestClient")
    private final RestClient presenceRestClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public PresenceClientImpl(
        @Qualifier("presenceRestClient") RestClient presenceRestClient,
        ServiceTokenProvider serviceTokenProvider
    ) {
        this.presenceRestClient = presenceRestClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Override
    public void registerSession(
        UUID userId,
        String sessionId,
        String instanceId
    ) {
        presenceRestClient
            .post()
            .uri("/api/v1/presence/sessions")
            .header("Authorization", authorizationHeader())
            .body(new RegisterPresenceSessionRequest(userId, sessionId, instanceId))
            .retrieve()
            .toBodilessEntity();
    }

    @Override
    public void removeSession(String sessionId) {
        presenceRestClient
            .delete()
            .uri("/api/v1/presence/sessions/{sessionId}", sessionId)
            .header("Authorization", authorizationHeader())
            .retrieve()
            .toBodilessEntity();
    }

    @Override
    public PresenceResponse getPresence(UUID userId) {
        ClientResponse<PresenceResponse> response = presenceRestClient
            .get()
            .uri("/api/v1/presence/users/{userId}", userId)
            .header("Authorization", authorizationHeader())
            .retrieve()
            .body(PRESENCE_RESPONSE_TYPE);

        return requireData(response);
    }

    @Override
    public List<PresenceResponse> getPresence(List<UUID> userIds) {
        ClientResponse<List<PresenceResponse>> response = presenceRestClient
            .post()
            .uri("/api/v1/presence/users/lookup")
            .header("Authorization", authorizationHeader())
            .body(new PresenceLookupRequest(userIds))
            .retrieve()
            .body(PRESENCE_LIST_RESPONSE_TYPE);

        return requireData(response);
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        ClientResponse<UserOnlineResponse> response = presenceRestClient
            .get()
            .uri("/api/v1/presence/users/{userId}/online", userId)
            .header("Authorization", authorizationHeader())
            .retrieve()
            .body(USER_ONLINE_RESPONSE_TYPE);

        return requireData(response).online();
    }

    private String authorizationHeader() {
        return serviceTokenProvider.authorizationHeader();
    }

    private <T> T requireData(ClientResponse<T> response) {
        if (response == null || response.data() == null) {
            throw new IllegalStateException("Presence service returned no data");
        }

        return response.data();
    }
}
