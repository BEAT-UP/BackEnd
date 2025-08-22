package com.BeatUp.BackEnd.Community.Post.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "author_id")
    private UUID authorId;

    @Column(nullable = false, name = "concert_id")
    private UUID concertId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 생성자
    protected Post(){}

    public Post(UUID authorId, UUID concertId, String title, String content){
        this.authorId = authorId;
        this.concertId = concertId;
        this.title = title;
        this.content = content;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getConcertId() {
        return concertId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
