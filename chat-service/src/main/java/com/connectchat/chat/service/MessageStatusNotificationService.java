package com.connectchat.chat.service;

import com.connectchat.chat.entity.MessageStatusInboxEvent;

public interface MessageStatusNotificationService {
    void notifyUsers(MessageStatusInboxEvent event);
}
