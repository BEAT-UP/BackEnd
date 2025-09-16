package com.BeatUp.BackEnd.Places;

import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import com.BeatUp.BackEnd.Places.controller.PlacesController;
import com.BeatUp.BackEnd.Places.dto.request.NearbySearchRequest;
import com.BeatUp.BackEnd.Places.dto.response.*;
import com.BeatUp.BackEnd.Places.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlacesControllerTest {

    @Mock
    private PlaceService placeService;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private PlacesController placesController;

    @Test
    @DisplayName("주변 장소 검색 성공")
    void getNearbyPlaces_Success() {
        // Given
        NearbySearchResponse mockResponse = createMockResponse();
        when(placeService.searchNearby(any(NearbySearchRequest.class)))
                .thenReturn(mockResponse);
        when(categoryMapper.isValidCategory("카페")).thenReturn(true);

        // When
        ResponseEntity<NearbySearchResponse> response = placesController.getNearbyPlaces(
                37.5665, 126.9780, 1000, List.of("카페"), null, 20, 0);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalCount());
        assertEquals("테스트 카페", response.getBody().getPlaces().get(0).getName());
    }

    @Test
    @DisplayName("카테고리 목록 조회")
    void getSupportedCategories_Success() {
        // When
        ResponseEntity<Map<String, Object>> response = placesController.getSupportedCategories();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("categories"));
    }

    private NearbySearchResponse createMockResponse() {
        PlaceResponse place = PlaceResponse.builder()
                .id("12345")
                .name("테스트 카페")
                .address("서울 강남구 테헤란로 123")
                .category(CategoryResponse.builder()
                        .name("카페")
                        .code("CE7")
                        .build())
                .location(LocationResponse.of(37.5665, 126.9780))
                .distance(DistanceResponse.builder()
                        .meters(300)
                        .displayText("내 위치에서 300m")
                        .build())
                .build();

        return NearbySearchResponse.builder()
                .places(List.of(place))
                .totalCount(1)
                .searchCenter(LocationResponse.of(37.5665, 126.9780))
                .cacheHit(false)
                .build();
    }
}
