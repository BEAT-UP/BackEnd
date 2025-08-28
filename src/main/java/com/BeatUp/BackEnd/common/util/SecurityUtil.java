package com.BeatUp.BackEnd.common.util;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Spring Security 관련 공통 유틸리티
 */
public final class SecurityUtil {

    private SecurityUtil(){}

    /**
     * 현재 인증된 사용자의 ID를 반환
     * JWT 토큰에서 추출한 userId 반환
     *
     * @return 현재 사용자 ID
     * @throws ResponseStatusException 인증되지 않는 경우 401 에러
     */
    public static UUID getCurrentUserId(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof UUID userId){
            return userId;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다");
    }
}
