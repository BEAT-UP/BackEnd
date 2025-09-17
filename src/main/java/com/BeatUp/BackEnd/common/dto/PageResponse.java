package com.BeatUp.BackEnd.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이징 응답 래퍼 클래스
 * 페이징이 필요한 API 응답을 표준화
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    
    /**
     * 실제 데이터 리스트
     */
    private List<T> content;
    
    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int page;
    
    /**
     * 페이지 크기
     */
    private int size;
    
    /**
     * 전체 요소 개수
     */
    private long totalElements;
    
    /**
     * 전체 페이지 개수
     */
    private int totalPages;
    
    /**
     * 첫 번째 페이지 여부
     */
    private boolean first;
    
    /**
     * 마지막 페이지 여부
     */
    private boolean last;
    
    /**
     * 현재 페이지의 요소 개수
     */
    private int numberOfElements;
    
    /**
     * 정렬 정보
     */
    private SortInfo sort;
    
    /**
     * 정렬 정보 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo {
        private boolean sorted;
        private boolean unsorted;
        private boolean empty;
    }
    
    /**
     * 빈 페이지 응답 생성
     */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .content(List.of())
                .page(0)
                .size(0)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .numberOfElements(0)
                .sort(SortInfo.builder()
                        .sorted(false)
                        .unsorted(true)
                        .empty(true)
                        .build())
                .build();
    }
    
    /**
     * 페이지 응답 생성 (Spring Data Page 인터페이스와 호환)
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .numberOfElements(content.size())
                .sort(SortInfo.builder()
                        .sorted(false)
                        .unsorted(true)
                        .empty(false)
                        .build())
                .build();
    }
}
