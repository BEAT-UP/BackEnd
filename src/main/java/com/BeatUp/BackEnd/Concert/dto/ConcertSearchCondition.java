package com.BeatUp.BackEnd.Concert.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConcertSearchCondition {
    private String query; // 이름 또는 장소 검색
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String region;
    private String genre;
}
