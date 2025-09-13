package com.BeatUp.BackEnd.Places.client;


import com.BeatUp.BackEnd.Places.dto.response.KakaoPlaceSearchResponse;
import com.BeatUp.BackEnd.Places.exception.KakaoApiException;
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

    public KakaoPlaceSearchResponse searchByCategory(
            double lat, double lng, int radius, String categoryCode, int size
    ){
        return kakaoWebClient
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if(response.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS)){
                        return Mono.error(KakaoApiException.rateLimitExceeded());
                    }
                    return response.bodyToMono(String.class)
                            .map(KakaoApiException::badRequest);
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(KakaoApiException.serverError(response.statusCode().value())))
                .bodyToMono(KakaoPlaceSearchResponse.class)
                .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(500))
                        .filter(this::isRetryableException))
                .doOnSuccess(response ->
                        log.debug("카카오맵 검색 성공 - 결과 수: {}",
                                response.getDocuments() != null ? response.getDocuments().size() : 0))
                .doOnError(error ->
                        log.error("카카오맵 API 호출 실패 - category: {}, error: {}",
                                categoryCode, error.getMessage()))
                .onErrorMap(TimeoutException.class, ex -> KakaoApiException.timeout())
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException)
                .block();
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
