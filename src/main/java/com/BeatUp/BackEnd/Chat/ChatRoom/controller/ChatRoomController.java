package com.BeatUp.BackEnd.Chat.ChatRoom.controller;

import com.BeatUp.BackEnd.Chat.ChatRoom.dto.request.CreateRoomRequest;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.response.RoomResponse;
import com.BeatUp.BackEnd.Chat.ChatRoom.service.ChatRoomService;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request){
        UUID createdBy = SecurityUtil.getCurrentUserId();
        RoomResponse response = chatRoomService.createRoom(createdBy, request);
        ApiResponse<RoomResponse> apiResponse = ApiResponse.success(response, "채팅방 생성 성공");
        return ResponseEntity.status(201).body(apiResponse);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinRoom(@PathVariable UUID roomId){
        UUID userId = SecurityUtil.getCurrentUserId();
        chatRoomService.joinRoom(userId, roomId);
        ApiResponse<Void> apiResponse = ApiResponse.success(null, "채팅방 참여 성공");
        return ResponseEntity.ok(apiResponse);
    }

}
