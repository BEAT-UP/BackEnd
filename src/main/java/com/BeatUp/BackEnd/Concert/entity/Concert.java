package com.BeatUp.BackEnd.Concert.entity;


import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.enums.DataSource;
import com.BeatUp.BackEnd.Concert.initializer.ConcertInitializer;
import com.BeatUp.BackEnd.Concert.logic.ConcertBusinessLogic;
import com.BeatUp.BackEnd.Concert.mapper.ConcertKopisMapper;
import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "concert")
public class Concert extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 200)
    private String venue;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(length = 50)
    private String genre;

    @Column(length = 1000)
    private String price;

    // KOPIS 연동 필드
    @Column(name = "kopis_id", unique = true, length = 20)
    private String kopisId;

    @Column(name = "facility_id", length = 20)
    private String facilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", length = 20)
    private DataSource dataSource = DataSource.MANAUAL;

    // 기본 공연 정보 확장
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 100)
    private String area;

    @Column(length = 20)
    private String state;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    // 상세 공연 정보
    @Column(name = "cast_info", length = 1000)
    private String cast;

    @Column(name = "crew_info", length = 1000)
    private String crewInfo;

    @Column(length = 100)
    private String runtime;

    @Column(name = "age_limit", length = 50)
    private String ageLimit;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "performance_time", length = 500)
    private String performanceTime;

    // 제작 정보
    @Column(length = 200)
    private String producer;

    @Column(length = 200)
    private String planner;

    @Column(length = 200)
    private String host;

    @Column(length = 200)
    private String organizer;

    // KOPIS 플래그 정보
    @Column(name = "is_open_run")
    private Boolean isOpenRun = false;

    @Column(name = "is_child_performance")
    private Boolean isChildPerformance = false;

    @Column(name = "is_daehakro")
    private Boolean isDaehakro = false;

    @Column(name = "is_festival")
    private Boolean isFestival = false;

    @Column(name = "is_musical_license")
    private Boolean isMusicalLicense = false;

    @Column(name = "is_musical_create")
    private Boolean isMusicalCreate = false;

    @Column(name = "is_visit")
    private Boolean isVisit = false;

    // 동기화 정보
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "kopis_updated_at")
    private LocalDateTime kopisUpdatedAt;

    // 기본 생성자
    public Concert() {}

    public Concert(String name, String venue, LocalDateTime startAt, String genre, String price) {
        this.name = name;
        this.venue = venue;
        this.startAt = startAt;
        this.genre = genre;
        this.price = price;

        ConcertInitializer.initializeNewFields(this, startAt);

    }

    // === 변환 메서드 ===
    public static Concert fromKopisData(KopisPerformanceDto dto) {
        return ConcertKopisMapper.fromKopisData(dto);
    }

    public void updateFromKopisData(KopisPerformanceDto dto) {
        ConcertKopisMapper.updateFromKopisData(this, dto);
    }

    // 비즈니스 로직
    public boolean isFromKopis(){
        return ConcertBusinessLogic.isFromKopis(this);
    }

    public boolean isOnging(){
        return ConcertBusinessLogic.isOngoing(this);
    }

    public LocalDateTime getStartAt(){
        return startDate != null ? startDate.atStartOfDay() : startAt;
    }

}
