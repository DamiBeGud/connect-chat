package com.connectchat.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
            + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
            + "org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration,"
            + "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration,"
            + "org.springframework.boot.data.cassandra.autoconfigure.DataCassandraRepositoriesAutoConfiguration,"
            + "org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration",
        "identity.jwt.secret=connect-chat-local-jwt-secret-must-be-at-least-32-bytes",
        "identity.jwt.issuer=http://localhost:8081",
    }
)
class ChatServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
