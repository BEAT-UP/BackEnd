package com.BeatUp.BackEnd.Places.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbySearchResponse {
    private List<PlaceResponse> places;
    private Integer totalCount;
    private LocationResponse searchCenter;
    private Boolean cacheHit;

    // 검색 메타 정보
    private List<String> appliedCategories;
    private Integer searchRadius;

    public NearbySearchResponse withCacheHit(Boolean cacheHit){
        this.cacheHit = cacheHit;
        return this;
    }
}
