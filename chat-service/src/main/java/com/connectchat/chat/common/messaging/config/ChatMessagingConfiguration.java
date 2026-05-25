package com.connectchat.chat.common.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatMessagingProperties.class)
public class ChatMessagingConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "chat.messaging",
        name = "private-message-listener-enabled",
        havingValue = "true"
    )
    Queue privateMessageQueue(ChatMessagingProperties properties) {
        return queue(properties.privateMessageQueue());
    }

    @Bean
    Queue statusRequestQueue(ChatMessagingProperties properties) {
        return queue(properties.statusRequestQueue());
    }

    @Bean
    Queue statusConfirmedQueue(ChatMessagingProperties properties) {
        return queue(properties.statusConfirmedQueue());
    }

    @Bean
    DirectExchange privateMessageExchange(ChatMessagingProperties properties) {
        return exchange(properties.privateMessageExchange());
    }

    @Bean
    DirectExchange groupMessageExchange(ChatMessagingProperties properties) {
        return exchange(properties.groupMessageExchange());
    }

    @Bean
    DirectExchange statusRequestExchange(ChatMessagingProperties properties) {
        return exchange(properties.statusRequestExchange());
    }

    @Bean
    DirectExchange statusConfirmedExchange(ChatMessagingProperties properties) {
        return exchange(properties.statusConfirmedExchange());
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "chat.messaging",
        name = "private-message-listener-enabled",
        havingValue = "true"
    )
    Binding privateMessageBinding(
        Queue privateMessageQueue,
        DirectExchange privateMessageExchange,
        ChatMessagingProperties properties
    ) {
        return binding(
            privateMessageQueue,
            privateMessageExchange,
            properties.privateMessageRoutingKey()
        );
    }

    @Bean
    Binding statusRequestBinding(
        Queue statusRequestQueue,
        DirectExchange statusRequestExchange,
        ChatMessagingProperties properties
    ) {
        return binding(
            statusRequestQueue,
            statusRequestExchange,
            properties.statusRequestRoutingKey()
        );
    }

    @Bean
    Binding statusConfirmedBinding(
        Queue statusConfirmedQueue,
        DirectExchange statusConfirmedExchange,
        ChatMessagingProperties properties
    ) {
        return binding(
            statusConfirmedQueue,
            statusConfirmedExchange,
            properties.statusConfirmedRoutingKey()
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
