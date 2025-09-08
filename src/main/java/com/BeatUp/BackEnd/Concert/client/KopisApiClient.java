package com.BeatUp.BackEnd.Concert.client;


import com.BeatUp.BackEnd.Concert.exception.KopisApiException;
import com.BeatUp.BackEnd.Concert.dto.response.KopisApiResponse;
import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.enums.KopisGenre;
import com.BeatUp.BackEnd.Concert.enums.KopisPerformanceState;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

@Component
@Slf4j
public class KopisApiClient {

    private final WebClient webClient;
    private final XmlMapper xmlMapper;
    private final String serviceKey;
    private final String responseFormat;
    private final int maxRetry;
    private final long delayBetweenCalls;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEFAULT_ROWS = 50;
    private static final int MAX_ROWS = 100;
    private static final int MAX_DAYS = 31;

    public KopisApiClient(WebClient.Builder webClientBuilder,
                          XmlMapper xmlMapper,
                          @Value("${kopis.api.base-url}") String baseUrl,
                          @Value("${kopis.api.service-key}") String serviceKey,
                          @Value("${kopis.api.response-format:json}") String responseFormat,
                          @Value("${kopis.api.max-retry:3}") int maxRetry,
                          @Value("${kopis.api.delay-between-calls:200}") long delayBetweenCalls){

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .codecs(configurer ->configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.xmlMapper = xmlMapper;
        this.serviceKey = serviceKey;
        this.responseFormat = responseFormat;
        this.maxRetry = maxRetry;
        this.delayBetweenCalls = delayBetweenCalls;
    }

    /**
     * 공연 목록 조회(비동기)
     */
    public Mono<List<KopisPerformanceDto>> searchPerformanceAsync(
            LocalDate startDate, LocalDate endDate,
            KopisGenre genre, KopisPerformanceState state,
            String area, int page, int rows
    ){
        // API 키 검증
        if(serviceKey == null || serviceKey.trim().isEmpty()){
            log.error("KOPIS API 서비스 키가 설정되지 않았습니다.");
            return Mono.just(List.of());
        }

        // 31일 제한: 30일 간격이 안전
        final LocalDate effectiveEndDate = endDate.isAfter(startDate.plusDays(30))
                ? startDate.plusDays(30)
                : endDate;

        // rows 제한
        final int effectiveRows = Math.min(rows, 10);

        // API 제약사항 검증
        if(startDate.plusDays(MAX_DAYS).isBefore(endDate)){
            log.warn("날짜 범위가 {}일을 초과합니다. 종료일을 조정합니다.", MAX_DAYS);
            endDate = startDate.plusDays(MAX_DAYS);
        }

        rows = Math.min(rows, MAX_ROWS);

        log.error("KOPIS API 공연 목록 조회 - startDate: {}, endDate: {}, genre: {}, page: {}, rows: {}",
                startDate, endDate, genre != null ? genre.getCode(): null, page, rows);


        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/pblprfr")
                            .queryParam("service", serviceKey)
                            .queryParam("stdate", startDate.format(DATE_FORMATTER))
                            .queryParam("eddate", effectiveEndDate.format(DATE_FORMATTER))
                            .queryParam("rows", effectiveRows)
                            .queryParam("cpage", page);

                    // 선택적 필터 파라미터
                    if(genre != null){
                        builder.queryParam("shcate", genre.getCode());
                    }
                    if(state != null){
                        builder.queryParam("prfstate", state.getCode());
                    }
                    if(area != null && !area.trim().isEmpty()){
                        builder.queryParam("signgucode", area);
                    }

                    return builder.build();
                })
                .accept(MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.error("KOPIS API 클라이언트 오류 - status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new KopisApiException("클라이언트 오류: " + body)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("KOPIS API 서버 오류 - status: {}", response.statusCode());
                    return Mono.error(new KopisApiException("서버 오류"));
                })
                .bodyToMono(String.class)  // ✅ 1단계: String으로 XML 받기
                .map(this::sanitizeXml)    // ✅ 2단계: XML 정리
                .flatMap(this::parseXmlToResponse)  // ✅ 3단계: 수동 파싱
                .map(this::extractPerformances)
                .retryWhen(Retry.backoff(maxRetry, Duration.ofMillis(delayBetweenCalls))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(retrySignal ->
                                log.warn("KOPIS API 재시도 {}/{} - 오류: {}",
                                        retrySignal.totalRetries() + 1, maxRetry,
                                        retrySignal.failure().getMessage())))
                .delayElement(Duration.ofMillis(delayBetweenCalls))
                .doOnSuccess(result ->
                        log.debug("KOPIS API 공연 목록 조회 완료 - 결과 수: {}", result.size()))
                .doOnError(error ->
                        log.error("KOPIS API 공연 목록 조회 실패", error))
                .onErrorReturn(List.of());
    }


    /**
     * 공연 상세 정보 조회 (비동기)
     */
    public Mono<Optional<KopisPerformanceDto>> getPerformanceDetailAsync(String mt20id){
        log.debug("KOPIS API 공연 상세 조회 - mt20id: {}", mt20id);

        if(mt20id == null || mt20id.trim().isEmpty()){
            return Mono.just(empty());
        }

        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/pblprfr/{mt20id}")
                            .queryParam("service", serviceKey);

                    if("json".equalsIgnoreCase(responseFormat)){
                        builder.queryParam("_type", "json");
                    }

                    return builder.build(mt20id);
                })
                .accept("json".equalsIgnoreCase(responseFormat) ?
                        MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if(response.statusCode() == HttpStatus.NOT_FOUND){
                        return Mono.empty();
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new KopisApiException("클라이언트 오류: " + body)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new KopisApiException("서버 오류")))
                .bodyToMono(KopisApiResponse.class)
                .map(response -> {
                    List<KopisPerformanceDto> performances = extractPerformances(response);
                    return performances.isEmpty() ?
                            Optional.<KopisPerformanceDto>empty():
                            Optional.of(performances.get(0));
                })
                .retryWhen(Retry.backoff(maxRetry, Duration.ofMillis(delayBetweenCalls))
                        .filter(this::isRetryableException))
                .delayElement(Duration.ofMillis(delayBetweenCalls))
                .doOnSuccess(result ->
                        log.debug("KOPIS API 공연 상세 조회 완료 - mt20id: {}, found: {}",
                                mt20id, result.isPresent()))
                .doOnError(error ->
                        log.error("KOPIS API 공연 상세 조회 실패 - mt20id: {}", mt20id, error))
                .onErrorReturn(Optional.empty());
    }

    // 동기 메서드들
    public List<KopisPerformanceDto> searchPerformances(
            LocalDate startDate, LocalDate endDate, KopisGenre genre, int page
    ){
        return searchPerformances(startDate, endDate, genre, null, null, page, DEFAULT_ROWS);
    }

    public List<KopisPerformanceDto> searchPerformances(
            LocalDate startDate, LocalDate endDate, KopisGenre genre,
            KopisPerformanceState state, String area, int page, int rows
    ){
        try{
            return searchPerformanceAsync(startDate, endDate, genre, state, area, page, rows).block();
        } catch (Exception e) {
            log.error("동기 공연 목록 조회 실패", e);
            return List.of();
        }
    }

    public Optional<KopisPerformanceDto> getPerformanceDetail(String mt20id){
        try{
            return getPerformanceDetailAsync(mt20id).block();
        } catch (Exception e) {
            log.error("동기 공연 조회 실패 - mt20id: {}", e);
            return Optional.empty();
        }
    }

    /**
     * XML 응답 처리 (% 문자, BOM 제거 등)
     */
    private String sanitizeXml(String xml){
        if(xml == null || xml.trim().isEmpty()){
            return xml;
        }

        String sanitized = xml.strip();

        // XML 응답 끝의 % 문자 제거
        if(sanitized.endsWith("%")){
            sanitized = sanitized.substring(0, sanitized.length() -1);
        }

        // BOM 제거
        if(sanitized.startsWith("\uFEFF")){
            sanitized = sanitized.substring(1);
        }

        return sanitized;
    }


    /**
     * XML 문자열을 KopisApiResponse 객체로 파싱
     */
    private Mono<KopisApiResponse> parseXmlToResponse(String xml) {
        return Mono.fromCallable(() -> {
            try {
                if (xml == null || xml.trim().isEmpty()) {
                    log.warn("빈 XML 응답입니다");
                    return new KopisApiResponse();
                }

                // ✅ XmlMapper로 수동 파싱
                KopisApiResponse response = xmlMapper.readValue(xml, KopisApiResponse.class);

                if (response == null) {
                    log.warn("XML 파싱 결과가 null입니다");
                    return new KopisApiResponse();
                }

                return response;

            } catch (Exception e) {
                log.error("XML 파싱 실패: {}", e.getMessage());
                log.debug("파싱 실패한 XML 내용: {}",
                        xml.length() > 500 ? xml.substring(0, 500) + "..." : xml);

                // ✅ 파싱 실패 시에도 빈 응답으로 처리하여 애플리케이션 중단 방지
                return new KopisApiResponse();
            }
        });
    }

    private List<KopisPerformanceDto> extractPerformances(KopisApiResponse response){
        if(response == null || response.getDbs() == null || response.getDbs().getDb() == null){
            return List.of();
        }

        return response.getDbs().getDb().stream()
                .filter(KopisPerformanceDto::isValidPerformance)
                .toList();
    }

    private boolean isRetryableException(Throwable throwable){
        if(throwable instanceof WebClientResponseException){
            WebClientResponseException ex = (WebClientResponseException) throwable;
            return ex.getStatusCode().is5xxServerError() ||
                    ex.getStatusCode() == HttpStatus.REQUEST_TIMEOUT;
        }

        return throwable instanceof java.net.ConnectException ||
                throwable instanceof java.util.concurrent.TimeoutException;
    }
}
