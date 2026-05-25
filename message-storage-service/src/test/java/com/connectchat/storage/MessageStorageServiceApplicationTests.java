package com.connectchat.storage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.connectchat.storage.repository.GroupStoredMessageRepository;
import com.connectchat.storage.repository.StorageInboxMessageRepository;
import com.connectchat.storage.repository.StorageStatusInboxMessageRepository;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.repository.UndeliveredGroupMessageRepository;
import com.connectchat.storage.repository.UndeliveredMessageRepository;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
            + "org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration,"
            + "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration,"
            + "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraRepositoriesAutoConfiguration",
    }
)
class MessageStorageServiceApplicationTests {

	@MockitoBean
	StorageInboxMessageRepository storageInboxMessageRepository;

	@MockitoBean
	StoredMessageRepository storedMessageRepository;

	@MockitoBean
	CqlSession cqlSession;

    @MockitoBean
    StorageStatusInboxMessageRepository storageStatusInboxMessageRepository;

    @MockitoBean
    UndeliveredMessageRepository undeliveredMessageRepository;

    @MockitoBean
    GroupStoredMessageRepository groupStoredMessageRepository;

    @MockitoBean
    UndeliveredGroupMessageRepository undeliveredGroupMessageRepository;

    @MockitoBean
    ConnectionFactory connectionFactory;

	@Test
	void contextLoads() {
	}

}
