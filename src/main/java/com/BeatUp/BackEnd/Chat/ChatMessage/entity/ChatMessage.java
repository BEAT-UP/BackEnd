package com.BeatUp.BackEnd.Chat.ChatMessage.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "room_id")
    private UUID roomId;

    @Column(name = "sender_id")
    private UUID senderId; // 시스템의 메시지의 경우 null

    @Column(nullable = false)
    private String type; // "TEXT"/"SYSTEM"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    protected ChatMessage(){}

    // 일반 메시지 생성자
    public ChatMessage(UUID roomId, UUID senderId, String content){
        this.roomId = roomId;
        this.senderId = senderId;
        this.type = "TEXT";
        this.content = content;
    }

    // 시스템 메시지 생성자
    public ChatMessage(UUID roomId, String content){
        this.roomId = roomId;
        this.senderId = null; // 시스템 메시지는 senderId가 없음
        this.type = "SYSTEM";
        this.content = content;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
