package com.BeatUp.BackEnd.Concert.service.sync;

import com.BeatUp.BackEnd.Concert.client.KopisApiClient;
import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.BeatUp.BackEnd.Concert.service.sync.DataWindowSplitter.*;


// 실제 동기화 실행 로직
@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSyncExecutor {

    private final KopisApiClient kopisApiClient;
    private final ConcertService concertService;
    private final DataWindowSplitter dataWindowSplitter;

    @Value("${kopis.api.delay-between-calls:200}")
    private long delayBetweenCalls;

    private static final int ROWS_PER_PAGE = 50;
    private static final int MAX_PAGES = 10;

    /**
     * 특정 날짜 범위 동기화 (31일 제한 고려한 윈도우 분할)
     */
    public int syncDateRange(LocalDate startDate, LocalDate endDate){
        if(startDate == null || endDate == null || endDate.isBefore(startDate)){
            log.warn("잘못된 날짜 범위: {} - {}", startDate, endDate);
            return 0;
        }

        int totalSynced = 0;
        List<DateWindow> windows = dataWindowSplitter.splitIntoWindows(startDate, endDate);

        for(DateWindow window: windows){
            totalSynced += syncSingleWindow(window.start, window.end);

            // 윈도우 간 지연
            if(delayBetweenCalls > 0){
                sleep(delayBetweenCalls);
            }
        }

        return totalSynced;
    }

    /**
     * 기존 공연 데이터 새로고침
     */
    public int refreshExistingConcerts(){
        List<Concert> needsUpdate = concertService.getConcertsNeedingSync();
        int refreshed = 0;

        for(Concert concert : needsUpdate){
            if(concert.getKopisId() == null) continue;

            try{
                var detailOpt = kopisApiClient.getPerformanceDetail(concert.getKopisId());
                if(detailOpt.isPresent()){
                    concertService.upsertFromKopis(detailOpt.get());
                    refreshed++;
                }

                sleep(delayBetweenCalls);
            } catch (Exception e) {
                log.warn("공연 새로고침 실패: {} - {}", concert.getKopisId(), e.getMessage());
            }
        }

        return refreshed;
    }

    /**
     * 단일 KOPIS ID 동기화(Fallback용)
     */
    public Concert syncSingleKopisId(String kopisId){
        if(kopisId == null || kopisId.trim().isEmpty()){
            return null;
        }

        try{
            var detailOpt = kopisApiClient.getPerformanceDetail(kopisId);
            if(detailOpt.isPresent()){
                Concert saved = concertService.upsertFromKopis(detailOpt.get());
                log.info("KOPIS ID 개별 동기화 성공: {} - {}", kopisId, saved.getName());
                return saved;
            }else{
                log.warn("KOPIS API에서 데이터를 찾을 수 없음: {}", kopisId);
                return null;
            }
        } catch (Exception e) {
            log.error("KOPIS ID 개별 동기화 실패: {}", kopisId, e);
            return null;
        }
    }

    /**
     * 단일 윈도우 (31일 이내) 동기화
     */
    private int syncSingleWindow(LocalDate startDate, LocalDate endDate){
        int windowSynced = 0;

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<KopisPerformanceDto> performances = kopisApiClient.searchPerformances(
                    startDate, endDate, null, null, null, page, ROWS_PER_PAGE
            );

            if (performances == null || performances.isEmpty()) {
                break;
            }

            List<Concert> savedConcerts = concertService.batchUpsertFromKopis(performances);
            windowSynced += savedConcerts.size();

            if (performances.size() < ROWS_PER_PAGE) break;
            sleep(delayBetweenCalls);
        }

        if (windowSynced > 0) {
            log.info("윈도우 동기화 완료 {}-{}: {}개", startDate, endDate, windowSynced);
        }
        return windowSynced;
    }

    private void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

}
