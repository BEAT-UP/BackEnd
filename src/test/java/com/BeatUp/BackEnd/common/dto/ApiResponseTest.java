package com.BeatUp.BackEnd.common.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successResponse_WithData_ShouldCreateSuccessResponse() {
        // Given
        String testData = "test data";
        
        // When
        ApiResponse<String> response = ApiResponse.success(testData);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals(testData, response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void successResponse_WithCustomMessage_ShouldCreateSuccessResponseWithCustomMessage() {
        // Given
        String testData = "test data";
        String customMessage = "Custom success message";
        
        // When
        ApiResponse<String> response = ApiResponse.success(testData, customMessage);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatus());
        assertEquals(customMessage, response.getMessage());
        assertEquals(testData, response.getData());
        assertNull(response.getError());
    }

    @Test
    void errorResponse_WithErrorInfo_ShouldCreateErrorResponse() {
        // Given
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(400)
                .code("INVALID_INPUT")
                .message("Invalid input value")
                .build();
        
        // When
        ApiResponse<String> response = ApiResponse.error(errorInfo);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatus());
        assertEquals("Invalid input value", response.getMessage());
        assertNull(response.getData());
        assertEquals(errorInfo, response.getError());
    }

    @Test
    void errorResponse_WithSimpleError_ShouldCreateErrorResponse() {
        // Given
        int status = 404;
        String code = "NOT_FOUND";
        String message = "Resource not found";
        
        // When
        ApiResponse<String> response = ApiResponse.error(status, code, message);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(code, response.getError().getCode());
    }
}
