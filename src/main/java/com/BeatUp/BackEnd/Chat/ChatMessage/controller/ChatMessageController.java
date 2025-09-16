package com.BeatUp.BackEnd.Chat.ChatMessage.controller;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.service.ChatMessageService;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    // 메시지 내역 조회 API
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable UUID roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime since){
        UUID userId = SecurityUtil.getCurrentUserId();
        List<ChatMessageResponse> messages = chatMessageService.getMessages(roomId, userId, since);
        return ResponseEntity.ok(messages);
    }

    // 딥링크 생성 API
    @GetMapping("/rooms/{roomId}/deeplink")
    private ResponseEntity<Map<String, String>> generateDeeplink(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "payment") String kind
    ){
        UUID userId = SecurityUtil.getCurrentUserId();
        String url = chatMessageService.generateDeeplink(roomId, userId, kind);

        return ResponseEntity.ok(Map.of("url", url));
    }

}
