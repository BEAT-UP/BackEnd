package com.BeatUp.BackEnd.Places.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {
    private Integer meters;
    private String displayText; // "내 위치에서 300m"
}
