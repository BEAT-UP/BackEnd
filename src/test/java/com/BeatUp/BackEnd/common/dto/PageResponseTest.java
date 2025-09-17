package com.BeatUp.BackEnd.common.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class PageResponseTest {

    @Test
    void emptyPage_ShouldCreateEmptyPageResponse() {
        // When
        PageResponse<String> response = PageResponse.empty();
        
        // Then
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(0, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(0, response.getNumberOfElements());
    }

    @Test
    void pageResponse_WithData_ShouldCreateCorrectPageResponse() {
        // Given
        List<String> content = List.of("item1", "item2", "item3");
        int page = 0;
        int size = 10;
        long totalElements = 25;
        
        // When
        PageResponse<String> response = PageResponse.of(content, page, size, totalElements);
        
        // Then
        assertEquals(content, response.getContent());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(3, response.getTotalPages()); // 25 / 10 = 2.5 -> 3
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(3, response.getNumberOfElements());
    }

    @Test
    void pageResponse_LastPage_ShouldSetLastFlagCorrectly() {
        // Given
        List<String> content = List.of("item1", "item2");
        int page = 2; // 3rd page (0-indexed)
        int size = 10;
        long totalElements = 25;
        
        // When
        PageResponse<String> response = PageResponse.of(content, page, size, totalElements);
        
        // Then
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }
}
