package com.connectchat.storage.common.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessageStorageMessagingProperties.class)
public class MessageStorageMessagingConfiguration {

    @Bean
    Queue privateMessageQueue(MessageStorageMessagingProperties properties) {
        return new Queue(properties.privateMessageQueue(), true);
    }

    @Bean
    DirectExchange privateMessageExchange(
        MessageStorageMessagingProperties properties
    ) {
        return new DirectExchange(properties.privateMessageExchange(), true, false);
    }

    @Bean
    Binding privateMessageBinding(
        Queue privateMessageQueue,
        DirectExchange privateMessageExchange,
        MessageStorageMessagingProperties properties
    ) {
        return BindingBuilder
            .bind(privateMessageQueue)
            .to(privateMessageExchange)
            .with(properties.privateMessageRoutingKey());
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter rabbitMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
            new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }
}
