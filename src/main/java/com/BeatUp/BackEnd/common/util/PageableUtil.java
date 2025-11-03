package com.BeatUp.BackEnd.common.util;

import com.BeatUp.BackEnd.common.config.PagingProperties;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;


/**
 * 페이징 처리 공통 유틸리티
 *
 * 주요 기능:
 * - 페이지 번호 및 크기 검증
 * - 정렬 필드 화이트리스트 검증
 * - 페이지 크기 제한 적용
 * - 기본값 처리
 */

@UtilityClass
public class PageableUtil {

    private static PagingProperties pagingProperties;

    /**
     * PagingProperties를 설정(필요시 @PostConstruct 등에서 주입)
     */
    public static void setPagingProperties(PagingProperties properties){
        pagingProperties = properties;
    }

    /**
     * 기본 Pageable 생성(검증 없음, 내부용)
     */
    public static Pageable createPageable(int page, int size){
        return PageRequest.of(page, size);
    }

    /**
     * 정렬 조건이 있는 Pageable 생성
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 크기
     * @param sortBy 정렬 필드
     * @param direction 정렬 방향 (ASC, DESC)
     * @param allowedSortFields 허용 가능한 정렬 필드 목록(화이트리스트)
     * @return 검증된 Pageable 객체
     * @throws BusinessException 허용되지 않은 정렬 필드인 경우
     */
    public static Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSortFields
    ){
        // 페이지 번호 검증
        if(page < 0){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "페이지 번호는 0 이상이어야 합니다.");
        }

        // 페이지 크기 검증 및 제한 적용
        int validatedSize = validateAndLimitSize(size);

        // 정렬 필드 검증
        String validatedSortBy = validateSortField(sortBy, allowedSortFields);

        // 정렬 병합 검증 및 기본값 처리
        Sort.Direction sortDirection = validateAndParseDirection(direction);

        return PageRequest.of(page, validatedSize, Sort.by(sortDirection, validatedSortBy));
    }

    /**
     * 기본 정렬 필드로 Pageable 생성(createdAt DESC)
     */
    public static Pageable createDefaultPageable(int page, int size, Set<String> allowedSortFields){
        return createPageable(
                page,
                validateAndLimitSize(size),
                getDefaultSortBy(),
                getDefaultDirection(),
                allowedSortFields
        );
    }

    /**
     * 정렬 필드 없이 기본 정렬로 Pageable 생성
     */
    public static Pageable createDefaultPageable(int page, int size){
        return createPageable(
                page,
                validateAndLimitSize(size),
                getDefaultSortBy(),
                getDefaultDirection(),
                Set.of(getDefaultSortBy())
        );
    }

    /**
     * 정렬 필드 없이 기본 정렬로 Pageable 생성
     */
    public static int validateAndLimitSize(int size){
        if(size < getMinSize()){
            return getDefaultSize();
        }
        if(size > getMaxSize()){
            return getMaxSize();
        }
        return size;
    }

    /**
     * 정렬 필드 검증
     *
     * @param sortBy 정렬 필드
     * @param allowedSortFields 허용된 정렬 필드 목록
     * @return 검증된 정렬 필드
     * @throws BusinessException 허용되지 않은 필드인 경우
     */
    public static String validateSortField(String sortBy, Set<String> allowedSortFields){
        // null이거나 비어있으면 기본값 사용
        if(sortBy == null || sortBy.trim().isEmpty()){
            return getDefaultSortBy();
        }

        // 정규화 (소문자 변환, 공백 제거)
        String normalizedSortBy = sortBy.trim().toLowerCase();

        // 화이트리스트 검증
        boolean isValid = allowedSortFields.stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(normalizedSortBy));

        if(!isValid){
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    String.format("허용되지 않은 정렬 필드입니다: %s. 허용 필드: %s", sortBy, allowedSortFields)
            );
        }

        // 원본 필드명 변환(데이터베이스 컬럼명과 일치해야 함)
        return sortBy.trim();
    }

    /**
     * 정렬 변환 검증 및 파싱
     */
    public static Sort.Direction validateAndParseDirection(String direction){
        if(direction == null || direction.trim().isEmpty()){
            return Sort.Direction.valueOf(getDefaultDirection());
        }

        String normalizedDirection = direction.trim().toUpperCase();

        try{
            return Sort.Direction.valueOf(normalizedDirection);
        }catch (IllegalArgumentException e){
            return Sort.Direction.valueOf(getDefaultDirection());
        }
    }

    /**
     * Spring Data의 Pageable 객체를 검증
     * 컨트롤러에서 직접 받은 Pageable 검증용
     */
    public static Pageable validatePageable(Pageable pageable, Set<String> allowedSortFields){
        if(pageable == null){
            return createDefaultPageable(0, getDefaultSize());
        }

        // 페이징 크기 제한 적용
        int validateSize = validateAndLimitSize(pageable.getPageSize());

        // 정렬 정보 검증
        Sort validateSort = pageable.getSort();
        if(validateSort != null && validateSort.isSorted()){
            validateSort.forEach(order -> {
                validateSortField(order.getProperty(), allowedSortFields);
            });
        }else{
            // 정렬이 없으면 기본 정렬 추가
            validateSort = Sort.by(Sort.Direction.valueOf(getDefaultDirection()), getDefaultSortBy());
        }

        return PageRequest.of(pageable.getPageNumber(), validateSize, validateSort);
    }

    // 설정값 조회 메서드들
    private static int getDefaultSize(){
        return pagingProperties != null ? pagingProperties.getDefaultSize() : 20;
    }

    private static int getMaxSize(){
        return pagingProperties != null ? pagingProperties.getMaxSize() : 100;
    }

    private static int getMinSize(){
        return pagingProperties != null ? pagingProperties.getMinSize() : 1;
    }

    private static String getDefaultSortBy(){
        return pagingProperties != null ? pagingProperties.getDefaultSortBy() : "createdAt";
    }

    private static String getDefaultDirection(){
        return pagingProperties != null ? pagingProperties.getDefaultDirection() : "DESC";
    }
}

