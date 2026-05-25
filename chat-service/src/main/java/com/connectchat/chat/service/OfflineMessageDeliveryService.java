package com.connectchat.chat.service;

import java.util.UUID;

public interface OfflineMessageDeliveryService {
    void deliverPendingMessages(UUID userId, String sessionId);

    void scheduleDelayedDelivery(UUID userId, String sessionId);
}
