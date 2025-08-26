package com.BeatUp.BackEnd.WebSocket;


import com.BeatUp.BackEnd.User.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

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
                            System.out.println("WebSocket JWT 인증 성공: " + userId);
                        }else {
                            System.out.println("WebSocket JWT 인증 실패");
                        }
                    }else {
                        System.out.println("WebSocket Authorization 헤더 없음");
                    }
                }
                return  message;
            }
        });
    }
}
