package com.BeatUp.BackEnd.Chat.ChatMessage.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessageResponse {

    private UUID id;
    private UUID roomId;
    private UUID senderId; // 시스템 메시지일 경우 null
    private String senderName;
    private String type; // "TEXT" | "SYSTEM"
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageResponse(){}

    public ChatMessageResponse(UUID id, UUID roomId, UUID senderId, String senderName,
                               String type, LocalDateTime createdAt, String content) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.createdAt = createdAt;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
