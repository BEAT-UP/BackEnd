package com.BeatUp.BackEnd.Chat.ChatRoom.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "chat_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_chat_member",
                columnNames = {"room_id", "user_id"} // 한 방에 한 사용자만 존재
        )
)
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "room_id")
    private UUID roomId;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String role = "MEMBER";

    @CreationTimestamp
    @Column(nullable = false, name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt; // 방 나간 시간

    // 생성자
    protected ChatMember(){}

    public ChatMember(UUID roomId, UUID userId){
        this.roomId = roomId;
        this.userId = userId;
    }

    // Getters

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    // Setters(필요한 경우)
    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }
}
