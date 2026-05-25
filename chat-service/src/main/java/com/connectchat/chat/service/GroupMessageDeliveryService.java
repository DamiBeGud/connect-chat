package com.connectchat.chat.service;

import com.connectchat.chat.common.messaging.GroupMessageCommand;

public interface GroupMessageDeliveryService {
    void deliver(GroupMessageCommand command);
}
