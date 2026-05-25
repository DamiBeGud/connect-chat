package com.connectchat.storage.repository;

import com.connectchat.storage.entity.GroupStoredMessage;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface GroupStoredMessageRepository
    extends CassandraRepository<GroupStoredMessage, UUID> {}
