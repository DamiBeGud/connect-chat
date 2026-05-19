package com.connectchat.identity.service;

import com.connectchat.identity.api.request.ServiceTokenRequest;
import com.connectchat.identity.api.response.ServiceTokenResponse;

public interface ServiceTokenService {
    ServiceTokenResponse issueToken(ServiceTokenRequest request);
}
