package com.BeatUp.BackEnd.Community.Post.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "post")
public class Post extends BaseEntity {

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

    // 생성자
    protected Post(){}

    public Post(UUID authorId, UUID concertId, String title, String content){
        this.authorId = authorId;
        this.concertId = concertId;
        this.title = title;
        this.content = content;
    }

}
