package com.BeatUp.BackEnd.Community.Post.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PostSearchCondition {

    private UUID concertId;
    private String query;
    private String status;
    private UUID authorId;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;

}
