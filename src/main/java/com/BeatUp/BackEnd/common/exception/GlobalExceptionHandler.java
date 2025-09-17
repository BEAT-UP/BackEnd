package com.BeatUp.BackEnd.common.exception;

import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.dto.ErrorInfo;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(e.getErrorCode().getStatus().value())
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    /**
     * Bean Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation exception occurred: {}", e.getMessage());
        
        Map<String, Object> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message("입력값이 올바르지 않습니다")
                .additionalInfo(fieldErrors)
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bind 예외 처리 (ModelAttribute 검증 실패)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.warn("Bind exception occurred: {}", e.getMessage());
        
        Map<String, Object> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message("입력값이 올바르지 않습니다")
                .additionalInfo(fieldErrors)
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 잘못된 HTTP 메서드 요청 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .code("METHOD_NOT_ALLOWED")
                .message("지원하지 않는 HTTP 메서드입니다")
                .details("요청된 메서드: " + e.getMethod() + ", 지원되는 메서드: " + String.join(", ", e.getSupportedMethods()))
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 404 Not Found 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("No handler found: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .code("NOT_FOUND")
                .message("요청한 리소스를 찾을 수 없습니다")
                .details("요청 URL: " + e.getRequestURL())
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 잘못된 요청 파라미터 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("MISSING_PARAMETER")
                .message("필수 파라미터가 누락되었습니다")
                .details("누락된 파라미터: " + e.getParameterName() + " (타입: " + e.getParameterType() + ")")
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch exception: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("TYPE_MISMATCH")
                .message("파라미터 타입이 올바르지 않습니다")
                .details("파라미터: " + e.getName() + ", 요청값: " + e.getValue() + ", 예상타입: " + e.getRequiredType().getSimpleName())
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * JSON 파싱 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Message not readable: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("INVALID_JSON")
                .message("요청 본문을 파싱할 수 없습니다")
                .details("올바른 JSON 형식으로 요청해주세요")
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument exception: {}", e.getMessage());
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("서버 내부 오류가 발생했습니다")
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 모든 예외 처리 (최후의 수단)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("예상치 못한 오류가 발생했습니다")
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(errorInfo);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
