package com.BeatUp.BackEnd.Places.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbySearchRequest {

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90도 이상이어야 합니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180도 이상이어야 합니다.")
    private Double lng;

    @Min(value = 100, message = "검색 반경은 최소 100m입니다.")
    @Max(value = 5000, message = "검색 반경은 최대 5km입니다.")
    @Builder.Default
    private Integer radius = 1000;

    private List<String> categories; // 사용자 친화적 카테고리("카페", "음식점")

    private Boolean openNow;

    @Min(value = 1, message = "결과 개수는 최소 1개입니다.")
    @Max(value = 50, message = "결과 개수는 최대 50개입니다.")
    @Builder.Default
    private Integer limit = 20;

    @Min(value = 0, message = "오프셋은 0이상이어야 합니다.")
    @Builder.Default
    private Integer offset = 0;

    // 캐싱용 키
    public String getCacheKey(){
        double latBucket = Math.round(lat * 10000.0)/10000.0;
        double lngBucket = Math.round(lng * 10000.0)/10000.0;
        String categoriesStr = categories != null ?
                String.join(",", categories.stream().sorted().toList()): "";

        return String.format("places:v2:%.4f:%.4f:%d:%s:%s:%d:%d",
                latBucket, lngBucket, radius, categoriesStr, openNow, limit, offset);
    }
}
