package com.BeatUp.BackEnd.Chat.ChatRoom.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;


public class RoomResponse {

    private UUID id;
    private String type;
    private UUID subjectId;
    private String title;
    private Integer maxMembers;
    private String status;
    private Long currentMembersCount;
    private LocalDateTime createdAt;

    // 기본 생성자
    public RoomResponse() {}

    // 생성자
    public RoomResponse(UUID id, String type, UUID subjectId, String title,
                        Integer maxMembers, String status, Long currentMembersCount,
                        LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.subjectId = subjectId;
        this.title = title;
        this.maxMembers = maxMembers;
        this.status = status;
        this.currentMembersCount = currentMembersCount;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCurrentMembersCount() { return currentMembersCount; }
    public void setCurrentMembersCount(Long currentMembersCount) { this.currentMembersCount = currentMembersCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}