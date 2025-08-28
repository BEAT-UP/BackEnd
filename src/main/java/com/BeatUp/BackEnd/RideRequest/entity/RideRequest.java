package com.BeatUp.BackEnd.RideRequest.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "ride_request")
public class RideRequest extends BaseEntity {

    @Column(nullable = false)
    private UUID userId; // JWT에서 추출한 사용자 ID

    @Column(nullable = false)
    private UUID concertId; // Concert API에서 검증

    @Column(nullable = false)
    private String direction; // "TO_VENUE" | "FROM_VENUE"

    @Column(nullable = false, name="dest_lat")
    private Double destLat; // 목적지 위도

    @Column(nullable = false, name="dest_lng")
    private Double destLng; // 목적지 경도

    @Column(name = "gender_pref")
    private String genderPref = "ANY"; // "ANY" | "MALE" | "FEMALE"

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Column(nullable = false, name="window_start")
    private LocalDateTime windowStart;

    @Column(nullable = false, name="window_end")
    private LocalDateTime windowEnd;

    @Column(nullable = false)
    private String status = "PENDING"; // "PENDING" | "MATCHED" | "CANCELED"

    @Column(name = "match_group_id")
    private UUID matchGroupId; // 매칭되면 설정

    // 기본 생성자
    protected RideRequest(){}

    // 생성자
    public RideRequest(
            UUID userId, UUID concertId, String direction,
            Double destLat, Double destLng, LocalDateTime windowStart, LocalDateTime windowEnd){
        this.userId = userId;
        this.concertId = concertId;
        this.direction = direction;
        this.destLat = destLat;
        this.destLng = destLng;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    // Setters(필요한 것만)
    public void setGenderPref(String genderPref) {
        this.genderPref = genderPref;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public void setMatchGroupId(UUID matchGroupId) {
        this.matchGroupId = matchGroupId;
    }

    @Override
    public String toString(){
        return "RideRequest(id=" + getId() + ", userId=" + userId + ",status=" + status + ")";
    }
}
