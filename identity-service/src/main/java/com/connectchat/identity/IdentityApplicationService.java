package com.connectchat.identity;

import com.connectchat.identity.dto.RegisterRequest;

public interface IdentityApplicationService {
    void register(RegisterRequest request);
}
