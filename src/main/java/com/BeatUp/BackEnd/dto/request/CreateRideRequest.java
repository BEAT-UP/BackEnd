package com.BeatUp.BackEnd.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreateRideRequest {

    @NotNull(message = "공연 ID는 필수입니다.")
    private UUID concertId;

    @NotBlank(message = "방향은 필수입니다")
    @Pattern(regexp = "TO_VENUE|FROM_VENUE", message = "방향은 TO_VENUE 또는 FROM_VENUE여야 합니다.")
    private String direction;

    @NotNull(message = "목적지 위도는 필수입니다.")
    @DecimalMin(value = "33.0", message = "위도는 33.0 이상이어야 합니다")
    @DecimalMax(value = "38.5", message = "위도는 38.5 이상이어야 합니다")
    private Double destLat;

    @NotNull(message = "목적지 경도는 필수입니다.")
    @DecimalMin(value = "124.0", message = "경도는 124.0이상이여야 합니다")
    @DecimalMax(value= "132.0", message = "경도는 132.0이하여야 합니다")
    private Double destLng;

    @Pattern(regexp = "ANY|MALE|FEMAL", message = "성별 선호는 ANY, MALE, FEMALE 중 하나여야 합니다")
    private String genderPref = "ANY";

    @Min(value = 10, message = "최소 나이는 10세 이상이여야 합니다")
    @Max(value = 100, message = "최소 나이는 100세 이하여야 합니다")
    private Integer ageMin;

    @Min(value = 10, message = "최대 나이는 10세 이상이여야 합니다")
    @Max(value = 100, message = "최대 나이는 100세 이하여야 합니다")
    private Integer ageMax;

    @NotNull(message = "탑승 시작 시간은 필수입니다")
    private LocalDateTime windowStart;

    @NotNull(message = "탑승 종료 시간은 필수입니다")
    private LocalDateTime windowEnd;

    // 기본 생성자
    public CreateRideRequest() {}

    // Getters and Setters
    public UUID getConcertId() { return concertId; }
    public void setConcertId(UUID concertId) { this.concertId = concertId; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }

    public Double getDestLng() { return destLng; }
    public void setDestLng(Double destLng) { this.destLng = destLng; }

    public String getGenderPref() { return genderPref; }
    public void setGenderPref(String genderPref) { this.genderPref = genderPref; }

    public Integer getAgeMin() { return ageMin; }
    public void setAgeMin(Integer ageMin) { this.ageMin = ageMin; }

    public Integer getAgeMax() { return ageMax; }
    public void setAgeMax(Integer ageMax) { this.ageMax = ageMax; }

    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }

    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
}
