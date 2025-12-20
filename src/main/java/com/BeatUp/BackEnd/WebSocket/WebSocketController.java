package com.BeatUp.BackEnd.WebSocket;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.request.ChatMessageRequest;
import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.service.ChatMessageService;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MonitoringUtil monitoringUtil;

    @MessageMapping("/rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable UUID roomId, @Payload Map<String, String> payload, Principal principal) {

        var sample = monitoringUtil.startApiCallTimer("websocket.message");
        try {
            // 1. JWT에서 사용자 ID 가져오기
            UUID senderId = getUserIdFromPrincipal(principal);

            if (senderId == null) {
                log.warn("인증되지 않은 사용자의 메시지 시도");
                monitoringUtil.recordApiCall(sample, "websocket.message", "unauthorized");
                monitoringUtil.recordApiCall("websocket.message", "unauthorized");
                return;
            }

            // 2. 메시지 내용 기본 검증
            String content = payload.get("content");
            if (content == null || content.trim().isEmpty()) {
                log.debug("빈 메시지 무시 - roomId: {}, senderId: {}", roomId, senderId);
                monitoringUtil.recordApiCall(sample, "websocket.message", "empty");
                return;
            }

            log.debug("메시지 처리 시작 - roomId: {}, senderId: {}", roomId, senderId);

            // 3. 슬래시 명령어 처리
            if (content.startsWith("/")) {
                chatMessageService.handleSlashCommand(senderId, roomId, content.trim());
                monitoringUtil.recordApiCall(sample, "websocket.message", "command");
                monitoringUtil.recordApiCall("websocket.message", "command");
                return; // 슬래시 명령어는 일반 메시지로 저장하지 않음
            }

            // 4. ChatService를 통해 메시지 저장 및 검증
            ChatMessageRequest request = new ChatMessageRequest(content.trim());
            ChatMessageResponse response = chatMessageService.sendMessage(roomId, senderId, request);

            // 5. 채팅방의 모든 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId, response);

            // 메시지 전송 성공 메트릭
            monitoringUtil.recordApiCall(sample, "websocket.message", "success");
            monitoringUtil.recordApiCall("websocket.message", "success");

            log.debug("메시지 브로드캐스트 완료 - roomId: {}, sender: {}", roomId, response.getSenderName());
        } catch (IllegalArgumentException e) {
            log.error("메시지 전송 실패 - roomId: {}, error: {}", roomId, e.getMessage());
            monitoringUtil.recordApiCall(sample, "websocket.message", "validation_error");
            monitoringUtil.recordApiCall("websocket.message", "validation_error");
            
            // 에러가 발신자에게만 전송
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            log.error("예상치 못한 오류 - roomId: {}", roomId, e);
            monitoringUtil.recordApiCall(sample, "websocket.message", "error");
            monitoringUtil.recordApiCall("websocket.message", "error");
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
