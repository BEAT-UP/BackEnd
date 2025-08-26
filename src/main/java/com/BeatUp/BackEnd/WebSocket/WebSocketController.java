package com.BeatUp.BackEnd.WebSocket;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.request.ChatMessageRequest;
import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatMessage.service.ChatMessageService;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class WebSocketController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable UUID roomId, @Payload Map<String, String> payload, Principal principal) {

        try {
            // 1. JWT에서 사용자 ID 가져오기
            UUID senderId = getUserIdFromPrincipal(principal);

            if (senderId == null) {
                System.out.println("인증되지 않는 사용자");
                return;
            }

            // 2. 메시지 내용 기본 검증
            String content = payload.get("content");
            if (content == null || content.trim().isEmpty()) {
                return;
            }

            System.out.println("메시지 처리 - 방: " + roomId + ", 발신자:" + senderId);

            // 3. ChatService를 통해 메시지 저장 및 검증
            ChatMessageRequest request = new ChatMessageRequest(content.trim());
            ChatMessageResponse response = chatMessageService.sendMessage(roomId, senderId, request);

            // 4. 채팅방의 모든 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId, response);

            System.out.println("메시지 브로드캐스트 완료 - " + response.getSenderName());
        } catch (IllegalArgumentException e) {
            System.err.println("메시지 전송 실패: " + e.getMessage());
            // 에러가 발신자에게만 전송
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            System.out.println("예상치 못한 오류:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private UUID getUserIdFromPrincipal(Principal principal){
        if(principal instanceof UsernamePasswordAuthenticationToken){
            Object principalObj = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if(principalObj instanceof UUID){
                return (UUID) principalObj;
            }
        }
        return null;
    }
}
