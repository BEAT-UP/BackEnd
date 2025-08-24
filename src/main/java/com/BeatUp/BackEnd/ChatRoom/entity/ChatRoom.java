package com.BeatUp.BackEnd.ChatRoom.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String type; // "MATCH" | "COMMUNITY"

    @Column(nullable = false, name = "subject_id")
    private UUID subjectId; // 매칭 그룹 ID 또는 게시글 ID

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "max_members")
    private Integer maxMembers = 4; // 기본 4명

    @Column(nullable = false)
    private String status = "OPEN"; // "OPEN" | "CLOSED"

    @Column(name = "ttl_at")
    private LocalDateTime ttlAt; // Time To Live(자동 삭제 시간)

    @Column(nullable = false, name = "created_by")
    private UUID createdBy; // 방 생성자 ID

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 생성자
    protected ChatRoom(){}

    public ChatRoom(String type, UUID subjectId, String title, UUID createdBy){
        this.type = type;
        this.subjectId = subjectId;
        this.title = title;
        this.createdBy = createdBy;
        // TTL은 필요에 따라 나중에 설정(예: 매칭방은 공연 종료 후 +24시간)
    }

    // Getters

    public String getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTtlAt() {
        return ttlAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setStatus(String status){this.status = status;}

    public void setTtlAt(LocalDateTime ttlAt) {
        this.ttlAt = ttlAt;
    }
}
