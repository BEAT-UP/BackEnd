package com.BeatUp.BackEnd.Community.Post.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "post")
@Getter
@Builder
@AllArgsConstructor
public class Post extends BaseEntity {

    @Column(nullable = false, name = "author_id")
    private UUID authorId;

    @Column(nullable = false, name = "concert_id")
    private UUID concertId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
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
