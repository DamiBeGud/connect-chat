package com.connectchat.chat.client;

import com.connectchat.chat.client.response.ServiceTokenResponse;

public interface ServiceTokenClient {
    ServiceTokenResponse issueServiceToken();
}
