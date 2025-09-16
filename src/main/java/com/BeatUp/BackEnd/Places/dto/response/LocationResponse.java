package com.BeatUp.BackEnd.Places.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Double lat;
    private Double lng;

    public static LocationResponse of(double lat, double lng){
        return LocationResponse.builder()
                .lat(lat)
                .lng(lng)
                .build();
    }
}

