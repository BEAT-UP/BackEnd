package com.BeatUp.BackEnd.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "concerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concert_seq")
    @SequenceGenerator(name = "concert_seq", sequenceName = "concerts_concert_id_seq", allocationSize = 1)
    @Column(name = "concert_id")
    private Long concertId;

    @Column(name = "venue_id", nullable = false, length = 15)
    private String venueId;

    @Column(name = "kopis_performance_code", length = 20)
    private String kopisPerformanceCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "artist", length = 100)
    private String artist;

    @Column(name = "genre_main", nullable = false, length = 20)
    private String genreMain;

    @Column(name = "genre_sub", length = 30)
    private String genreSub;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "age_rating", length = 20)
    private String ageRating;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Concert(String venueId, String kopisPerformanceCode, String title, String artist,
                   String genreMain, String genreSub, LocalDate startDate, LocalDate endDate,
                   String ageRating, Integer runtimeMinutes){
        this.venueId = venueId;
        this.kopisPerformanceCode = kopisPerformanceCode;
        this.title = title;
        this.artist = artist;
        this.genreMain = genreMain;
        this.genreSub = genreSub;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ageRating = ageRating;
        this.runtimeMinutes = runtimeMinutes;
    }
}
