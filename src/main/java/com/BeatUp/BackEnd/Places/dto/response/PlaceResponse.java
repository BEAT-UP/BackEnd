package com.BeatUp.BackEnd.Places.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {
    private String id; // 카카오 place_id
    private String name;
    private String address;
    private String phone;

    private CategoryResponse category;
    private LocationResponse location;
    private DistanceResponse distance;
    private String placeUrl;

}
