package com.connectchat.chat.service;

import com.connectchat.chat.common.messaging.PrivateMessageCommand;

public interface MessageDeliveryService {
    void deliver(PrivateMessageCommand command);
}
