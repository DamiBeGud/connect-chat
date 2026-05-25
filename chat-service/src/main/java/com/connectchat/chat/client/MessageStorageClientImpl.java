package com.connectchat.chat.client;

import com.connectchat.chat.client.response.UndeliveredMessageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MessageStorageClientImpl implements MessageStorageClient {

    private static final ParameterizedTypeReference<
        List<UndeliveredMessageResponse>
    > UNDELIVERED_MESSAGES_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient messageStorageRestClient;

    public MessageStorageClientImpl(
        @Qualifier("messageStorageRestClient") RestClient messageStorageRestClient
    ) {
        this.messageStorageRestClient = messageStorageRestClient;
    }

    @Override
    public List<UndeliveredMessageResponse> getUndeliveredMessages(
        UUID userId,
        int limit
    ) {
        List<UndeliveredMessageResponse> response = messageStorageRestClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/v1/messages/users/{userId}/undelivered")
                        .queryParam("limit", limit)
                        .build(userId)
            )
            .retrieve()
            .body(UNDELIVERED_MESSAGES_TYPE);

        if (response == null) {
            throw new IllegalStateException(
                "Message storage service returned no undelivered messages data"
            );
        }

        return response;
    }
}
