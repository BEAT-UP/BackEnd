package com.BeatUp.BackEnd.FCM.service.producer;

import com.BeatUp.BackEnd.FCM.config.RabbitMQConfig;
import com.BeatUp.BackEnd.FCM.dto.FcmNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 채팅 알림 실행
     */
    public void sendChatNotification(FcmNotificationMessage message){
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FCM_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_CHAT,
                message
        );
        log.debug("FCM 채팅 알림 실행 - userId: {}",
                message.getUserId());
    }

    /**
     * 매칭 알림 발행
     */
    public void sendMatchNotification(FcmNotificationMessage message){
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FCM_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_MATCH,
                message
        );
        log.debug("FCM 매칭 알림 발행 - userId: {}", message.getUserId());
    }

    /**
     * 실패한 알림을 실패 큐로 이동
     */
    public void sendToFailedQueue(FcmNotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FCM_FAILED_QUEUE,
                message
        );
        log.warn("FCM 알림 실패 큐로 이동 - userId: {}, retryCount: {}",
                message.getUserId(), message.getRetryCount());
    }
}
