package com.BeatUp.BackEnd.WebSocket;


import com.BeatUp.BackEnd.User.service.JwtService;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MonitoringUtil monitoringUtil;
    private final JwtService jwtService;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public WebSocketConfig(MonitoringUtil monitoringUtil, JwtService jwtService) {
        this.monitoringUtil = monitoringUtil;
        this.jwtService = jwtService;
        // 활성 연결 수 Gauge 등록
        monitoringUtil.registerGauge("websocket.connections.active", activeConnections);
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic"); // 구독 경로
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 경로
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 개발 환경용
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel){
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    // WebSocket 연결 시 JWT 토큰 검증
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    if(authToken != null && authToken.startsWith("Bearer")){
                        String token = authToken.substring(7);
                        UUID userId = jwtService.getUserIdFromToken(token);
                        if(userId != null){
                            // 인증 정보를 WebSocket 세션에 저장
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                            accessor.setUser(auth);
                            activeConnections.incrementAndGet();
                            log.info("WebSocket 연결 성공 - userId: {}", userId);
                            monitoringUtil.recordApiCall("websocket.connection", "success");
                        }else {
                            log.warn("WebSocket JWT 인증 실패");
                            monitoringUtil.recordApiCall("websocket.connection", "auth_failed");
                        }
                    }else {
                        log.warn("WebSocket Authorization 헤더 없음");
                        monitoringUtil.recordApiCall("websocket.connection", "no_auth");
                    }
                }else if (StompCommand.DISCONNECT.equals(accessor.getCommand())){
                    activeConnections.decrementAndGet();
                    log.debug("WebSocket 연결 해제 - 현재 활성 연결: {}", activeConnections.get());
                    monitoringUtil.recordApiCall("websocket.connection", "disconnect");
                }else if(StompCommand.SEND.equals(accessor.getCommand())){
                    monitoringUtil.recordApiCall("websocket.message", "received");
                }
                return  message;
            }
        });
    }
}
