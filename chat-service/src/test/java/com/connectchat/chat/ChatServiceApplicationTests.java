package com.connectchat.chat;

import com.connectchat.chat.repository.InboxMessageRepository;
import com.connectchat.chat.repository.MessageStatusInboxEventRepository;
import com.connectchat.chat.repository.MessageStatusOutboxEventRepository;
import com.connectchat.chat.repository.OutboxMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
class ChatServiceApplicationTests {

	@MockitoBean
	OutboxMessageRepository outboxMessageRepository;

	@MockitoBean
	InboxMessageRepository inboxMessageRepository;

    @MockitoBean
    MessageStatusOutboxEventRepository messageStatusOutboxEventRepository;

    @MockitoBean
    MessageStatusInboxEventRepository messageStatusInboxEventRepository;

    @MockitoBean
    ConnectionFactory connectionFactory;

	@Test
	void contextLoads() {
	}

}
