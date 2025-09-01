package com.BeatUp.BackEnd.Concert.exception;


/**
 * KOPIS API 호출 중 발생하는 예외를 처리하기 위한 커스텀 예외 클래스
 */
public class KopisApiException extends RuntimeException{

    public KopisApiException(String message){
        super(message);
    }

    public KopisApiException(String message, Throwable cause){
        super(message, cause);
    }
}
