package com.BeatUp.BackEnd.Community.Post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreatePostRequest {

    @NotNull(message = "공연 ID는 필수입니다")
    private UUID concertId;

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 2, max = 200, message = "제목은 2~200자여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 10, max = 2000, message = "내용은 10~2000자여야 합니다")
    private String content;

    // 생성자
    public CreatePostRequest(){}

    // Getters and Setters
    public UUID getConcertId(){return concertId;}
    public void setConcertId(UUID concertId){this.concertId = concertId;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
