package com.connectchat.storage.api;

import com.connectchat.storage.api.request.GroupMessageStatusUpdateRequest;
import com.connectchat.storage.api.response.UndeliveredMessageResponse;
import com.connectchat.storage.service.MessageStorageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageStorageController {

    private final MessageStorageService messageStorageService;

    @GetMapping("/users/{userId}/undelivered")
    public List<UndeliveredMessageResponse> getUndeliveredMessages(
        @PathVariable UUID userId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return messageStorageService
            .findUndeliveredMessages(userId, limit)
            .stream()
            .map(UndeliveredMessageResponse::from)
            .toList();
    }

    @PostMapping("/group/{messageId}/recipients/{recipientId}/status")
    public void updateGroupMessageStatus(
        @PathVariable UUID messageId,
        @PathVariable UUID recipientId,
        @RequestBody GroupMessageStatusUpdateRequest request
    ) {
        messageStorageService.updateGroupRecipientStatus(
            messageId,
            recipientId,
            request.status()
        );
    }
}
