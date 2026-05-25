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
        return queue(properties.privateMessageQueue());
    }

    @Bean
    Queue groupMessageQueue(MessageStorageMessagingProperties properties) {
        return queue(properties.groupMessageQueue());
    }

    @Bean
    Queue statusRequestQueue(MessageStorageMessagingProperties properties) {
        return queue(properties.statusRequestQueue());
    }

    @Bean
    DirectExchange privateMessageExchange(
        MessageStorageMessagingProperties properties
    ) {
        return exchange(properties.privateMessageExchange());
    }

    @Bean
    DirectExchange groupMessageExchange(
        MessageStorageMessagingProperties properties
    ) {
        return exchange(properties.groupMessageExchange());
    }

    @Bean
    DirectExchange statusRequestExchange(
        MessageStorageMessagingProperties properties
    ) {
        return exchange(properties.statusRequestExchange());
    }

    @Bean
    DirectExchange statusConfirmedExchange(
        MessageStorageMessagingProperties properties
    ) {
        return exchange(properties.statusConfirmedExchange());
    }

    @Bean
    Binding privateMessageBinding(
        Queue privateMessageQueue,
        DirectExchange privateMessageExchange,
        MessageStorageMessagingProperties properties
    ) {
        return binding(
            privateMessageQueue,
            privateMessageExchange,
            properties.privateMessageRoutingKey()
        );
    }

    @Bean
    Binding groupMessageBinding(
        Queue groupMessageQueue,
        DirectExchange groupMessageExchange,
        MessageStorageMessagingProperties properties
    ) {
        return binding(
            groupMessageQueue,
            groupMessageExchange,
            properties.groupMessageRoutingKey()
        );
    }

    @Bean
    Binding statusRequestBinding(
        Queue statusRequestQueue,
        DirectExchange statusRequestExchange,
        MessageStorageMessagingProperties properties
    ) {
        return binding(
            statusRequestQueue,
            statusRequestExchange,
            properties.statusRequestRoutingKey()
        );
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

    private Queue queue(String name) {
        return new Queue(name, true);
    }

    private DirectExchange exchange(String name) {
        return new DirectExchange(name, true, false);
    }

    private Binding binding(Queue queue, DirectExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
