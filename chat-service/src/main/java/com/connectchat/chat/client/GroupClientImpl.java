package com.connectchat.chat.client;

import com.connectchat.chat.client.response.ClientResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroupClientImpl implements GroupClient {

    private static final ParameterizedTypeReference<
        ClientResponse<List<UUID>>
    > MEMBER_IDS_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient groupRestClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public GroupClientImpl(
        @Qualifier("groupRestClient") RestClient groupRestClient,
        ServiceTokenProvider serviceTokenProvider
    ) {
        this.groupRestClient = groupRestClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Override
    public List<UUID> getGroupMemberIds(UUID groupId) {
        ClientResponse<List<UUID>> response = groupRestClient
            .get()
            .uri("/api/v1/groups/{groupId}/member-ids", groupId)
            .header("Authorization", serviceTokenProvider.authorizationHeader())
            .retrieve()
            .body(MEMBER_IDS_RESPONSE_TYPE);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Group service returned no member ids");
        }

        return response.data();
    }
}
