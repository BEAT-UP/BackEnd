package com.BeatUp.BackEnd.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.paging")
public class PagingProperties {
    /**
     * 기본 페이지 크기
     */
    private int defaultSize = 20;

    /**
     * 최대 페이지 크기
     */
    private int maxSize = 100;

    /**
     * 최소 페이지 크기
     */
    private int minSize = 1;

    /**
     * 기본 정렬 필드
     */
    private String defaultSortBy = "createdAt";

    /**
     * 기본 정렬 방향 (ASC, DESC)
     */
    private String defaultDirection = "DESC";
}
