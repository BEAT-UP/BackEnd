package com.BeatUp.BackEnd.Community.Post.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class PostResponse {

    private UUID id;
    private UUID authorId;
    private UUID concertId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public PostResponse(){}

    public PostResponse(UUID id, UUID authorId, UUID concertId, String title,
                        String content, LocalDateTime createdAt){
        this.id = id;
        this.authorId = authorId;
        this.concertId = concertId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public UUID getConcertId() {
        return concertId;
    }

    public void setConcertId(UUID concertId) {
        this.concertId = concertId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
