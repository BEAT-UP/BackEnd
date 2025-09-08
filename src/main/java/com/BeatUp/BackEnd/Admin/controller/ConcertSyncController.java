package com.BeatUp.BackEnd.Admin.controller;


import com.BeatUp.BackEnd.Concert.service.ConcertSyncService;
import com.BeatUp.BackEnd.Concert.service.sync.SyncStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin/")
@RequiredArgsConstructor
public class ConcertSyncController {

    private final ConcertSyncService concertSyncService;

    /**
     * 현재 동기화 목록 조회
     */
    @GetMapping("/status")
    public ResponseEntity<SyncStatus> getSyncStatus(){
        SyncStatus status = concertSyncService.getCurrentStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 수동 초기 동기화 트리거
     */
    @PostMapping("/inital")
    public ResponseEntity<Map<String, Object>> triggerInitialSync(){
        int synced = concertSyncService.performInitialSync();
        return ResponseEntity.ok(Map.of(
                "message", "초기 동기화 완료",
                "synced", synced
        ));
    }

    /**
     * 날짜 범위 지정 동기화
     */
    @PostMapping("/range")
    public ResponseEntity<Map<String, Object>> syncDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate
            ){

        int synced = concertSyncService.syncDateRange(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", String.format("범위 동기화 완료: %s ~ %s", startDate, endDate),
                "synced", synced,
                "startDate", startDate,
                "endDate", endDate
        ));
    }

    /**
     * 단일 KOPIS ID 동기화
     */
    @PostMapping("/single/{kopisId}")
    public ResponseEntity<Map<String, Object>> syncSingle(@PathVariable String kopisId){
        var concert = concertSyncService.syncByKopisId(kopisId);
        return ResponseEntity.ok(Map.of(
                "kopisId", kopisId,
                "success", concert != null,
                "concertName", concert != null ? concert.getName() : null
        ));
    }
}
