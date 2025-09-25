package com.BeatUp.BackEnd;

import com.BeatUp.BackEnd.common.util.GeoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeoUtilsTest {

    @Test
    @DisplayName("거리 계산 - 같은 좌표")
    void calculateDistance_SameCoordinates_ReturnsZero(){
        // Given
        double lat = 37.5665;
        double lng = 126.9780;

        // When
        double distance = GeoUtils.calculateDistance(lat, lng, lat, lng);

        // Then
        assertEquals(0.0, distance, 0.001);
    }
}
