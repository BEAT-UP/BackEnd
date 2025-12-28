package com.BeatUp.BackEnd.FCM.service.consumer;


import com.BeatUp.BackEnd.FCM.config.RabbitMQConfig;
import com.BeatUp.BackEnd.FCM.dto.FcmNotificationMessage;
import com.BeatUp.BackEnd.FCM.service.FcmService;
import com.BeatUp.BackEnd.FCM.service.producer.FcmNotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(RabbitTemplate.class)
public class FcmNotificationConsumer {

    private final FcmService fcmService;
    private final FcmNotificationProducer producer;

    /**
     * FCM 알림 큐에서 메시지 수신 및 처리
     * 새로운 알림 타입 추가 시 별도 Consumer 메서드 불필요
     * -> routing key만 추가하면 자동으로 라우팅
     */
    @RabbitListener(
            queues = RabbitMQConfig.FCM_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleNotification(
            @Payload FcmNotificationMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
            ){
        try{
            log.debug("FCM 알림 수신 - userId: {}, type: {}, routingKey: {}", message.getUserId(), message.getType(), routingKey);

            // FCM 알림 전송
            fcmService.sendNotification(
                    message.getUserId(),
                    message.getTitle(),
                    message.getBody(),
                    message.getData()
            );

            log.info("FCM 알림 처리 완료 - userId: {}, type: {}", message.getUserId(), message.getType());
        }catch(Exception e){
            log.error("FCM 알림 처리 실패 - userId: {}, type: {}, error: {}",
                    message.getUserId(), message.getType(), e.getMessage(), e);

            // 재시도 로직
            handleFailure(message, e);
        }
    }

    /**
     * 실패 처리 및 재시도 로직
     */
    private void handleFailure(FcmNotificationMessage message, Exception e){
        int maxRetries = 3;

        if(message.getRetryCount() < maxRetries){
            // 재시도 카운트 증가
            message.setRetryCount(message.getRetryCount() + 1);

            log.warn("FCM 알림 재시도 - userId: {}, retryCount: {}",
                    message.getUserId(), message.getRetryCount());

            // 실패 큐로 이동
            producer.sendToFailedQueue(message);
        }else{
            log.error("FCM 알림 최대 재시도 초과 - userId: {}, type: {}",
                    message.getUserId(), message.getType());

            // 최종 실패 처리
            producer.sendToFailedQueue(message);
        }
    }
}
