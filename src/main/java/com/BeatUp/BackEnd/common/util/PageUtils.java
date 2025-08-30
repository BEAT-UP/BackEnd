package com.BeatUp.BackEnd.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이징 처리를 위한 공통 유틸리티 클래스
 * 일관된 페이징 로직과 보안을 제공
 */
@UtilityClass
public class PageUtils {

    // 상수 정의
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    /**
     * 기본 페이징 생성(최신순 정렬)
     * 가장 자주 사용되는 패턴
     */
    public static Pageable createDefaultPageable(int page, int size){
        int validatedSize = validatePageSize(size);
        return PageRequest.of(page, validatedSize,
                Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
    }

    /**
     * 페이지 크기 검증 및 보정
     */
    private static int validatePageSize(int size){
        if(size <= 0){
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

}
