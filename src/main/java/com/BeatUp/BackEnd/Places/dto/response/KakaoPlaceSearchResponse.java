package com.BeatUp.BackEnd.Places.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPlaceSearchResponse {

    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("documents")
    private List<PlaceDocument> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta{
        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("pageable_count")
        private Integer pageableCount;

        @JsonProperty("is_end")
        private Boolean isEnd;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDocument{
        @JsonProperty("id")
        private String id;

        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("category_group_code")
        private String categoryGroupCode;

        @JsonProperty("category_group_name")
        private String categoryGroupName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        @JsonProperty("x")
        private String x; // 경도

        @JsonProperty("y")
        private String y; // 위도

        @JsonProperty("place_url")
        private String placeUrl;

        @JsonProperty("distance")
        private String distance;
    }

}
