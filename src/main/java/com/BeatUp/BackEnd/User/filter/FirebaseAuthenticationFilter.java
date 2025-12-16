package com.BeatUp.BackEnd.User.filter;


import com.BeatUp.BackEnd.User.entity.UserAccount;
import com.BeatUp.BackEnd.User.service.FirebaseAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private final FirebaseAuthService firebaseAuthService;
    private final ObjectMapper objectMapper;

    public FirebaseAuthenticationFilter(FirebaseAuthService firebaseAuthService){
        this.firebaseAuthService = firebaseAuthService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String idToken = authHeader.substring(7).trim();
            
            if(idToken.isEmpty()){
                filterChain.doFilter(request, response);
                return;
            }

            try{
                // Firebase ID 토큰 검증
                FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(idToken);

                // 사용자 정보 조회 또는 생성
                UserAccount user = firebaseAuthService.findOrCreateUser(decodedToken);

                // SecurityContext에 사용자 ID 설정(기존 SecurityUtil 호환)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getId(), null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }catch (FirebaseAuthException e){
                // 토큰 검증 실패 시 401 반환
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Firebase 토큰 검증 실패", e.getMessage());
                return;
            } catch (Exception e) {
                // 기타 오류 시 500 반환
                writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 오류", e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * JSON 형태의 에러 응답을 안전하게 전송
     */
    private void writeJsonError(HttpServletResponse response, int status, String error, String message){

        try{
            if(response.isCommitted()){
                logger.warn("응답이 이미 커밋되어 에러 메시지를 전송할 수 없습니다.");
                return;
            }

            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            Map<String, String> errorResponse = Map.of(
                    "error", error,
                    "message", message != null ? message : "알 수 없는 오류가 발생했습니다."
            );

            objectMapper.writeValue(response.getWriter(), errorResponse);
        }catch (IOException e){
            logger.error("JSON 에러 응답 전송 실패: {}", e.getMessage());
        }
    }
}
