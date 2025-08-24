package com.BeatUp.BackEnd.Chat.ChatRoom.controller;


import com.BeatUp.BackEnd.Chat.ChatRoom.dto.request.CreateRoomRequest;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.response.RoomResponse;
import com.BeatUp.BackEnd.Chat.ChatRoom.service.ChatRoomService;
import com.BeatUp.BackEnd.Chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request){
        UUID createdBy = getCurrentUserId();
        RoomResponse response = chatRoomService.createRoom(createdBy, request);
        return ResponseEntity.status(201).body(response);
    }


    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable UUID roomId){
        UUID userId = getCurrentUserId();
        chatRoomService.joinRoom(userId, roomId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // 현재 사용자 ID 가져오기
    private UUID getCurrentUserId(){
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
