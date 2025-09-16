package com.BeatUp.BackEnd.Places.controller;

import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import com.BeatUp.BackEnd.Places.dto.request.NearbySearchRequest;
import com.BeatUp.BackEnd.Places.dto.response.NearbySearchResponse;
import com.BeatUp.BackEnd.Places.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Places API", description = "공연장 주변 장소 검색")
public class PlacesController {

    private final PlaceService placeService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "주변 장소 검색")
    @GetMapping("/nearby")
    public ResponseEntity<NearbySearchResponse> getNearbyPlaces(
            @RequestParam
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
            Double lat,

            @RequestParam
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
            Double lng,

            @RequestParam(defaultValue = "1000")
            @Min(100) @Max(5000)
            Integer radius,

            @RequestParam(required = false)
            List<String> categories,

            @RequestParam(required = false)
            Boolean openNow,

            @RequestParam(defaultValue = "20")
            @Min(1)@Max(50)
            Integer limit,

            @RequestParam(defaultValue = "0")
            @Min(0)
            Integer offset
    ){
        log.info("주변 장소 검색 - lat: {}, lng: {}, radius: {}m", lat, lng, radius);

        // 유효한 카테고리만 필터링
        List<String> validCategories = categories != null ?
                categories.stream()
                        .filter(categoryMapper::isValidCategory)
                        .toList() : null;

        NearbySearchRequest request = NearbySearchRequest.builder()
                .lat(lat)
                .lng(lng)
                .radius(radius)
                .categories(validCategories)
                .openNow(openNow)
                .limit(limit)
                .offset(offset)
                .build();

        NearbySearchResponse response = placeService.searchNearby(request);

        log.info("검색 완료 - 결과: {}개", response.getTotalCount());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지원 카테고리 목록")
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getSupportedCategories(){
        List<String> categories = List.of(
                "음식점", "카페", "패스트푸드", "편의시설", "편의점", "주차", "오락거리"
        );

        return ResponseEntity.ok(Map.of("categories", categories));
    }
}
