package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketDeliveryPayloadConverter {

    private final ObjectMapper objectMapper;

    public String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                "Failed to serialize websocket delivery payload",
                exception
            );
        }
    }

    public Object deserialize(WebSocketDeliveryTask task) {
        try {
            return objectMapper.readValue(
                task.getPayload(),
                payloadClass(task.getType())
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                "Failed to deserialize websocket delivery payload taskId=" +
                task.getId(),
                exception
            );
        }
    }

    private Class<?> payloadClass(WebSocketDeliveryTaskType type) {
        return switch (type) {
            case PRIVATE_MESSAGE -> PrivateMessageResponse.class;
            case PRIVATE_MESSAGE_STATUS -> PrivateMessageStatusResponse.class;
        };
    }
}
