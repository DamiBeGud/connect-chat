package com.connectchat.chat.repository;

import com.connectchat.chat.entity.GroupOutboxRecipient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupOutboxRecipientRepository
    extends JpaRepository<GroupOutboxRecipient, UUID> {
    boolean existsByMessageIdAndRecipientId(UUID messageId, UUID recipientId);

    List<GroupOutboxRecipient> findByMessageIdOrderByCreatedAtAsc(UUID messageId);
}
