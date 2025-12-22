package com.BeatUp.BackEnd.Concert.service;



import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.service.sync.ConcertSyncExecutor;
import com.BeatUp.BackEnd.Concert.service.sync.SyncStatus;
import com.BeatUp.BackEnd.Concert.service.sync.SyncStatusProvider;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
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
    private final MonitoringUtil monitoringUtil;
    private final AtomicInteger syncInProgress = new AtomicInteger(0);

    /**
     * 매일 새벽 2시 전체 동기화 실행
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async("concertSyncTaskExecutor")
    public void scheduledFullSync(){
        var sample = monitoringUtil.startApiCallTimer("concert.sync");

        try{
            if(!tryStartSync()){
                monitoringUtil.recordApiCall("concert.sync", "skipped");
                log.warn("동기화가 이미 진행 중입니다.");
                return;
            }

            log.info("KOPIS 전체 데이터 동기화 시작");

            LocalDate startDate = LocalDate.now().minusMonths(1); // 과거 1개월
            LocalDate endDate = LocalDate.now().plusMonths(6); // 미래 6개월

            int totalSynced = syncExecutor.syncDateRange(startDate, endDate);

            log.info("KOPIS 전체 동기화 완료 - {}개 처리", totalSynced);

            // 성공 매트릭
            monitoringUtil.recordApiCall(sample, "concert.sync", "success");
            monitoringUtil.recordApiCall("concert.sync", "success");

        } catch (Exception e) {
            monitoringUtil.recordApiCall(sample, "concert.sync", "failure");
            monitoringUtil.recordApiCall("concert.sync", "failure");
            log.error("전체 동기화 중 오류 발생", e);
        } finally {
            finishSync();
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

        var sample = monitoringUtil.startApiCallTimer("concert.sync.incremental");
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

            monitoringUtil.recordApiCall(sample, "concert.sync.incremental", "success");
            monitoringUtil.recordApiCall("concert.sync.incremental", "success");
        }catch (Exception e){
            monitoringUtil.recordApiCall(sample, "concert.sync.incremental", "failure");
            monitoringUtil.recordApiCall("concert.sync.incremental", "failure");
            log.error("증분 동기화 중 오류 발생", e);
        }finally {
            finishSync();
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

        var sample = monitoringUtil.startApiCallTimer("concert.sync.initial");
        log.info("KOPIS 초기 동기화 시작");

        try{
            LocalDate today = LocalDate.now();
            LocalDate syncStart = today;
            LocalDate syncEnd = today.plusMonths(3); // 향후 3개월

            int synced = syncExecutor.syncDateRange(syncStart, syncEnd);
            log.info("KOPIS 초기 동기화 완료 - {}개 처리", synced);

            monitoringUtil.recordApiCall(sample, "concert.sync.initial", "success");
            monitoringUtil.recordApiCall("concert.sync.initial", "success");
            return synced;
        } catch (Exception e) {
            monitoringUtil.recordApiCall(sample, "concert.sync.initial", "failure");
            monitoringUtil.recordApiCall("concert.sync.initial", "failure");
            log.error("초기 동기화 중 오류 발생", e);
            return 0;
        } finally {
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

        var sample = monitoringUtil.startApiCallTimer("concert.sync.manual");
        try{
            int synced = syncExecutor.syncDateRange(startDate, endDate);
            monitoringUtil.recordApiCall(sample, "concert.sync.manual", "success");
            monitoringUtil.recordApiCall("concert.sync.manual", "success");
            return synced;
        } catch (Exception e) {
            monitoringUtil.recordApiCall(sample, "concert.sync.manual", "failure");
            monitoringUtil.recordApiCall("concert.sync.manual", "failure");
            log.error("수동 동기화 중 오류 발생", e);
            return 0;
        } finally {
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
    public void resetSyncStatus(){
        syncInProgress.set(0);
    }

    private boolean tryStartSync(){
        return syncInProgress.compareAndSet(0, 1);
    }

    private void finishSync(){
        syncInProgress.set(0);
    }











}
