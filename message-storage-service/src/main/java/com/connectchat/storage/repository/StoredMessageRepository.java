package com.connectchat.storage.repository;

import com.connectchat.storage.entity.StoredMessage;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface StoredMessageRepository
    extends CassandraRepository<StoredMessage, UUID> {}
