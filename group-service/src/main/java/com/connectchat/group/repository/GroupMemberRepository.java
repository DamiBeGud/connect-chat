package com.connectchat.group.repository;

import com.connectchat.group.entity.GroupMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository
    extends JpaRepository<GroupMember, UUID> {
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMember> findByGroupIdOrderByCreatedAtAsc(UUID groupId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
