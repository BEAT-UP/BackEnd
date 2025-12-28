package com.BeatUp.BackEnd.FCM.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ConnectionFactory.class)
public class RabbitMQConfig {

    // Exchange 이름
    public static final String FCM_EXCHANGE = "fcm.exchange";

    // Queue 이름들
    public static final String FCM_QUEUE = "fcm.notifications.queue";
    public static final String FCM_FAILED_QUEUE = "fcm.notifications.failed.queue";

    // Routing Key
    public static final String ROUTING_KEY_CHAT = "fcm.chat";
    public static final String ROUTING_KEY_MATCH = "fcm.match";


    /**
     * JSON 메시지 컨버터
     */
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 설정
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Listener Factory 설정
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    /**
     * Exchange 생성(Topic Exchange)
     */
    @Bean
    public TopicExchange fcmExchange(){
        return ExchangeBuilder
                .topicExchange(FCM_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 일반 알림 큐
     */
    @Bean
    public Queue fcmQueue(){
        return QueueBuilder
                .durable(FCM_QUEUE)
                .build();
    }

    /**
     * 실패한 알림 큐
     */
    @Bean
    public Queue fcmFailedQueue(){
        return QueueBuilder
                .durable(FCM_FAILED_QUEUE).build();
    }

    /**
     * Binding: 채팅 알림
     */
    @Bean
    public Binding fcmChatQueueBinding() {
        return BindingBuilder
                .bind(fcmQueue())
                .to(fcmExchange())
                .with(ROUTING_KEY_CHAT);
    }

    /**
     * Binding: 매칭 알림
     */
    @Bean
    public Binding fcmMatchQueueBinding(){
        return BindingBuilder
                .bind(fcmQueue())
                .to(fcmExchange())
                .with(ROUTING_KEY_MATCH);
    }
}
