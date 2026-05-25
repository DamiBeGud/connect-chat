package com.connectchat.chat.client;

import com.connectchat.chat.client.response.UndeliveredMessageResponse;
import java.util.List;
import java.util.UUID;

public interface MessageStorageClient {
    List<UndeliveredMessageResponse> getUndeliveredMessages(
        UUID userId,
        int limit
    );
}
