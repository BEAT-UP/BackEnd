package com.BeatUp.BackEnd.common.util;

import com.BeatUp.BackEnd.common.config.PagingProperties;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilTest {

    @BeforeEach
    void setUp() {
        // 테스트용 설정
        PagingProperties properties = new PagingProperties();
        properties.setDefaultSize(20);
        properties.setMaxSize(100);
        properties.setMinSize(1);
        properties.setDefaultSortBy("createdAt");
        properties.setDefaultDirection("DESC");
        PageableUtil.setPagingProperties(properties);
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트 - 최대값 초과 시 제한")
    void validateAndLimitSize_WhenExceedsMaxSize_ShouldLimit() {
        // Given
        int oversized = 999;

        // When
        int result = PageableUtil.validateAndLimitSize(oversized);

        // Then
        assertEquals(100, result);
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트 - 최소값 미만 시 기본값")
    void validateAndLimitSize_WhenBelowMinSize_ShouldUseDefault() {
        // Given
        int undersized = -5;

        // When
        int result = PageableUtil.validateAndLimitSize(undersized);

        // Then
        assertEquals(20, result); // 기본값
    }

    @Test
    @DisplayName("정렬 필드 검증 테스트 - 허용된 필드")
    void validateSortField_WithAllowedField_ShouldReturnField() {
        // Given
        Set<String> allowedFields = Set.of("name", "createdAt", "startDate");
        String validField = "name";

        // When
        String result = PageableUtil.validateSortField(validField, allowedFields);

        // Then
        assertEquals("name", result);
    }

    @Test
    @DisplayName("정렬 필드 검증 테스트 - 허용되지 않은 필드 시 예외")
    void validateSortField_WithInvalidField_ShouldThrowException() {
        // Given
        Set<String> allowedFields = Set.of("name", "createdAt");
        String invalidField = "password"; // 허용되지 않은 필드

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> PageableUtil.validateSortField(invalidField, allowedFields)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("허용되지 않은 정렬 필드"));
    }

    @Test
    @DisplayName("정렬 필드 검증 테스트 - null이면 기본값 반환")
    void validateSortField_WithNull_ShouldReturnDefault() {
        // Given
        Set<String> allowedFields = Set.of("name", "createdAt");

        // When
        String result = PageableUtil.validateSortField(null, allowedFields);

        // Then
        assertEquals("createdAt", result); // 기본값
    }

    @Test
    @DisplayName("정렬 방향 검증 테스트 - ASC")
    void validateAndParseDirection_WithASC_ShouldReturnASC() {
        // When
        Sort.Direction result = PageableUtil.validateAndParseDirection("ASC");

        // Then
        assertEquals(Sort.Direction.ASC, result);
    }

    @Test
    @DisplayName("정렬 방향 검증 테스트 - 대소문자 무시")
    void validateAndParseDirection_WithLowerCase_ShouldReturnCorrect() {
        // When
        Sort.Direction result = PageableUtil.validateAndParseDirection("desc");

        // Then
        assertEquals(Sort.Direction.DESC, result);
    }

    @Test
    @DisplayName("정렬 방향 검증 테스트 - 잘못된 값은 기본값")
    void validateAndParseDirection_WithInvalid_ShouldReturnDefault() {
        // When
        Sort.Direction result = PageableUtil.validateAndParseDirection("invalid");

        // Then
        assertEquals(Sort.Direction.DESC, result); // 기본값
    }

    @Test
    @DisplayName("validatePageable - 페이지 크기 제한 적용")
    void validatePageable_WithOversizedPage_ShouldLimit() {
        // Given
        Set<String> allowedFields = Set.of("name", "createdAt");
        Pageable oversizedPageable = PageRequest.of(0, 999);

        // When
        Pageable result = PageableUtil.validatePageable(oversizedPageable, allowedFields);

        // Then
        assertEquals(100, result.getPageSize()); // 최대값으로 제한
    }

    @Test
    @DisplayName("validatePageable - 허용되지 않은 정렬 필드 시 예외")
    void validatePageable_WithInvalidSortField_ShouldThrowException() {
        // Given
        Set<String> allowedFields = Set.of("name", "createdAt");
        Pageable pageableWithInvalidSort = PageRequest.of(
            0, 
            20, 
            Sort.by(Sort.Direction.DESC, "password") // 허용되지 않은 필드
        );

        // When & Then
        assertThrows(
            BusinessException.class,
            () -> PageableUtil.validatePageable(pageableWithInvalidSort, allowedFields)
        );
    }

    @Test
    @DisplayName("validatePageable - null이면 기본값")
    void validatePageable_WithNull_ShouldReturnDefault() {
        // Given
        Set<String> allowedFields = Set.of("createdAt");

        // When
        Pageable result = PageableUtil.validatePageable(null, allowedFields);

        // Then
        assertNotNull(result);
        assertEquals(20, result.getPageSize());
        assertEquals(0, result.getPageNumber());
    }
}
