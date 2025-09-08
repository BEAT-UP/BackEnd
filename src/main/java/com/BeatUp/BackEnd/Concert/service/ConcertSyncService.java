package com.BeatUp.BackEnd.Concert.service;



import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.service.sync.ConcertSyncExecutor;
import com.BeatUp.BackEnd.Concert.service.sync.SyncStatus;
import com.BeatUp.BackEnd.Concert.service.sync.SyncStatusProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcertSyncService {

    private final ConcertSyncExecutor syncExecutor;
    private final SyncStatusProvider statusProvider;
    private final AtomicInteger syncInProgress = new AtomicInteger(0);

    /**
     * 매일 새벽 2시 전체 동기화 실행
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async("concertSyncTaskExecutor")
    public void scheduledFullSync(){
        if(!tryStartSync()){
            log.warn("이미 동기화가 진행 중입니다. 건너뜁니다.");
            return;
        }

        log.info("KOPIS 전체 데이터 동기화 시작");

        try{
            syncInProgress.incrementAndGet();

            LocalDate startDate = LocalDate.now().minusMonths(1); // 과거 1개월
            LocalDate endDate = LocalDate.now().plusMonths(6); // 미래 6개월

            int totalSynced = syncExecutor.syncDateRange(startDate, endDate);

            log.info("KOPIS 전체 동기화 완료 - {}개 처리", totalSynced);
        } catch (Exception e) {
            log.error("전체 동기화 중 오류 발생", e);
        } finally {
            syncInProgress.decrementAndGet();
        }
    }

    /**
     * 매시간 증분 동기화 (최근 변경 데이터만)
     */
    @Scheduled(fixedRate = 3600000)
    @Async("concertSyncTaskExecutor")
    public void scheduledIncrementalSync(){
        if(syncInProgress.get() > 0){
            return;
        }

        log.debug("KOPIS 증분 동기화 시작");

        try{
            syncInProgress.incrementAndGet();

            // 1) 동기화 필요한 기존 데이터 업데이트
            int updatedCount = syncExecutor.refreshExistingConcerts();

            // 2) 최근 1주일 신규 데이터 확인
            LocalDate recentStart = LocalDate.now().minusDays(7);
            LocalDate recentEnd = LocalDate.now().plusWeeks(2);
            int newCount = syncExecutor.syncDateRange(recentStart, recentEnd);

            if(updatedCount > 0 || newCount > 0){
                log.info("KOPIS 증분 동기화 완료 - 업데이트: {}개, 신규: {}개", updatedCount, newCount);
            }
        }catch (Exception e){
            log.error("증분 동기화 중 오류 발생", e);
        }finally {
            syncInProgress.decrementAndGet();
        }
    }

    /**
     * 수동 초기 동기화 트리거
     */
    public int performInitialSync(){
        if(!tryStartSync()){
            log.warn("이미 동기화가 진행 중입니다.");
            return 0;
        }

        log.info("KOPIS 초기 동기화 시작");

        try{
            LocalDate today = LocalDate.now();
            LocalDate syncStart = today;
            LocalDate syncEnd = today.plusMonths(3); // 향후 3개월

            int synced = syncExecutor.syncDateRange(syncStart, syncEnd);
            log.info("KOPIS 초기 동기화 완료 - {}개 처리", synced);

            return synced;
        }finally {
            finishSync();
        }
    }

    /**
     * 특정 날짜 범위 동기화
     */
    public int syncDateRange(LocalDate startDate, LocalDate endDate){
        if(!tryStartSync()){
            log.warn("이미 동기화가 진행 중입니다.");
            return 0;
        }

        try{
            return syncExecutor.syncDateRange(startDate, endDate);
        }finally {
            finishSync();
        }
    }

    /**
     * 단일 KOPIS ID 동기화 (Fallback 용)
     */
    public Concert syncByKopisId(String kopisId){
        return syncExecutor.syncSingleKopisId(kopisId);
    }

    /**
     * 현재 동기화 상태 조회
     */
    public SyncStatus getCurrentStatus(){
        return statusProvider.getCurrentStatus(syncInProgress.get() > 0);
    }

    // 동기화 상태 관리
    private boolean tryStartSync(){
        return syncInProgress.compareAndSet(0, 1);
    }

    private void finishSync(){
        syncInProgress.set(0);
    }











}
