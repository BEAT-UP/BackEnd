package com.BeatUp.BackEnd.Concert.dto;


import com.BeatUp.BackEnd.Concert.enums.DataSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ConcertSearchCondition {
    private String query; // 통합 검색어 (이름, 장소, 출연진)
    private LocalDate date; // 특정 날짜
    private LocalDate startDate; // 시작 날짜 범위
    private LocalDate endDate; // 종료 날짜 범위
    private String genre; // 장르
    private String state; // 공연 상태
    private String area; // 지역
    private DataSource dataSource; // 데이터 출처

    // 플래그 검색
    private Boolean isOpenRun;
    private Boolean isChildPerformance;
    private Boolean isDaehakro;
    private Boolean isFestival;

    // 정렬 옵션
    private String sortBy; // name, startDate, createdAt
    private String sortDirection; // asc, desc
}
