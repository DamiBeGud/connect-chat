package com.connectchat.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatInstanceInfo {

    private final String podId;
    private final String serviceName;

    public ChatInstanceInfo(
        @Value("${chat.instance.pod-id}") String podId,
        @Value("${chat.instance.service-name}") String serviceName
    ) {
        this.podId = podId;
        this.serviceName = serviceName;
    }

    public String getPodId() {
        return podId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getInstanceId() {
        return serviceName + ":" + podId;
    }
}
