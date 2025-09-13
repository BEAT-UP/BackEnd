package com.BeatUp.BackEnd.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Bean Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e){
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f-> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Validation error");

        return ResponseEntity.badRequest().body(Map.of(
                "code", "INVALID_INPUT",
                "message", msg
        ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e)
    {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(Map.of(
                        "code", e.getErrorCode().name(),
                        "message", e.getMessage(),
                        "status", e.getErrorCode().getStatus().value()
                ));
    }

    // 비즈니스 로직 에러 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(Map.of(
                "code", "INVALID_INPUT",
                "message", e.getMessage()
        ));
    }

    // 일반 런타임 에러 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", "INTERNAL_ERROR",
                "message", e.getMessage()
        ));
    }
}
