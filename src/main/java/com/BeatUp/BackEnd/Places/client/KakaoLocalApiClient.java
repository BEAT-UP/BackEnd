package com.BeatUp.BackEnd.Places.client;


import com.BeatUp.BackEnd.Places.dto.response.KakaoPlaceSearchResponse;
import com.BeatUp.BackEnd.Places.exception.KakaoApiException;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoLocalApiClient {

    public final WebClient kakaoWebClient;
    public final MonitoringUtil monitoringUtil;

    public KakaoPlaceSearchResponse searchByCategory(
            double lat, double lng, int radius, String categoryCode, int size
    ){
        Timer.Sample sample = monitoringUtil.startApiCallTimer("kakao.places");

        try{
            KakaoPlaceSearchResponse response = kakaoWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryCode)
                            .queryParam("x", lng)
                            .queryParam("y", lat)
                            .queryParam("radius", radius)
                            .queryParam("sort", "distance")
                            .queryParam("size", Math.min(size, 15))
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, httpResponse -> {
                        if(httpResponse.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS)){
                            monitoringUtil.recordRateLimit("kakao.places");
                            return Mono.error(KakaoApiException.rateLimitExceeded());
                        }
                        return httpResponse.bodyToMono(String.class)
                                .map(KakaoApiException::badRequest);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, httpResponse ->
                            Mono.error(KakaoApiException.serverError(httpResponse.statusCode().value())))
                    .bodyToMono(KakaoPlaceSearchResponse.class)
                    .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(500))
                            .filter(this::isRetryableException))
                    .doOnSuccess(apiResponse -> {
                        log.debug("카카오맵 검색 성공 - 결과 수: {}",
                                apiResponse.getDocuments() != null ? apiResponse.getDocuments().size() : 0);
                        monitoringUtil.recordApiCall(sample, "kakao.places", "success");
                        monitoringUtil.recordApiCall("kakao.places", "success");
                    })
                    .doOnError(error -> {
                        log.error("카카오맵 API 호출 실패 - category: {}, error: {}",
                                categoryCode, error.getMessage());
                        
                        // Rate Limit 체크
                        if(error instanceof KakaoApiException){
                            KakaoApiException ex = (KakaoApiException)error;
                            if(ex.getErrorCode() == ErrorCode.KAKAO_API_RATE_LIMIT){
                                monitoringUtil.recordRateLimit("kakao.places");
                            }
                        }
                        
                        monitoringUtil.recordApiCall(sample, "kakao.places", "failure");
                        monitoringUtil.recordApiCall("kakao.places", "failure");
                    })
                    .onErrorMap(TimeoutException.class, ex -> KakaoApiException.timeout())
                    .onErrorMap(WebClientResponseException.class, this::mapWebClientException)
                    .block();
            
            return response;
        } catch (Exception e) {
            // 예외 발생 시 메트릭 기록
            monitoringUtil.recordApiCall(sample, "kakao.places", "error");
            monitoringUtil.recordApiCall("kakao.places", "error");
            throw e;
        }
    }

    public KakaoPlaceSearchResponse searchByKeyword(String keyword, int size) {
        Timer.Sample sample = monitoringUtil.startApiCallTimer("kakao.places.keyword");

        try{
            KakaoPlaceSearchResponse response = kakaoWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", keyword)
                            .queryParam("size", Math.min(size, 15))
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, httpResponse -> {
                        if(httpResponse.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS)){
                            monitoringUtil.recordRateLimit("kakao.places.keyword");
                            return Mono.error(KakaoApiException.rateLimitExceeded());
                        }
                        return httpResponse.bodyToMono(String.class)
                                .map(KakaoApiException::badRequest);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, httpResponse ->
                            Mono.error(KakaoApiException.serverError(httpResponse.statusCode().value())))
                    .bodyToMono(KakaoPlaceSearchResponse.class)
                    .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(500))
                            .filter(this::isRetryableException))
                    .doOnSuccess(apiResponse -> {
                        log.debug("카카오맵 키워드 검색 성공 - keyword: {}, 결과 수: {}",
                                keyword, apiResponse.getDocuments() != null ? apiResponse.getDocuments().size() : 0);
                        monitoringUtil.recordApiCall(sample, "kakao.places.keyword", "success");
                        monitoringUtil.recordApiCall("kakao.places.keyword", "success");
                    })
                    .doOnError(error -> {
                        log.error("카카오맵 키워드 검색 실패 - keyword: {}, error: {}",
                                keyword, error.getMessage());
                        
                        // Rate Limit 체크
                        if(error instanceof KakaoApiException){
                            KakaoApiException ex = (KakaoApiException)error;
                            if(ex.getErrorCode() == ErrorCode.KAKAO_API_RATE_LIMIT){
                                monitoringUtil.recordRateLimit("kakao.places.keyword");
                            }
                        }
                        
                        monitoringUtil.recordApiCall(sample, "kakao.places.keyword", "failure");
                        monitoringUtil.recordApiCall("kakao.places.keyword", "failure");
                    })
                    .onErrorMap(TimeoutException.class, ex -> KakaoApiException.timeout())
                    .onErrorMap(WebClientResponseException.class, this::mapWebClientException)
                    .block();
            
            return response;
        } catch (Exception e) {
            // 예외 발생 시 메트릭 기록
            monitoringUtil.recordApiCall(sample, "kakao.places.keyword", "error");
            monitoringUtil.recordApiCall("kakao.places.keyword", "error");
            throw e;
        }
    }

    public boolean isRetryableException(Throwable throwable){
        if(throwable instanceof WebClientResponseException){
            WebClientResponseException ex = (WebClientResponseException) throwable;
            return ex.getStatusCode().is5xxServerError() ||
                    ex.getStatusCode() == HttpStatus.REQUEST_TIMEOUT;
        }
        return throwable instanceof TimeoutException;
    }

    public KakaoApiException mapWebClientException(WebClientResponseException ex){
        if(ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS){
            return KakaoApiException.rateLimitExceeded();
        }else if(ex.getStatusCode().is5xxServerError()){
            return KakaoApiException.serverError(ex.getStatusCode().value());
        }else{
            return KakaoApiException.generalError("카카오맵 API 호출 실패: " + ex.getMessage(), ex);
        }
    }
}
