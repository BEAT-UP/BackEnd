package com.BeatUp.BackEnd.Chat.ChatRoom.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public class CreateRoomRequest {

    @NotBlank(message = "채팅방 타입은 필수입니다")
    @Pattern(regexp = "MATCH|COMMUNITY", message = "타입은 MATCH 또는 COMMUNITY여야 합니다")
    private String type;

    @NotNull(message = "주제 ID는 필수입니다")
    private UUID subjectId; // 매칭 그룹 ID 또는 게시글 ID

    @NotBlank(message = "채팅방 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "제목은 1~100자여야 합니다")
    private String title;

    @Min(value = 2, message = "최소 인원은 2명입니다")
    @Max(value = 10, message = "최대 인원은 10명입니다")
    private Integer maxMembers = 4;

    // 기본 생성자
    public CreateRoomRequest(){}

    // Getters and Setters

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
}
