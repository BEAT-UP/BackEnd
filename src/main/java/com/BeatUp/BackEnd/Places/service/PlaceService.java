package com.BeatUp.BackEnd.Places.service;

import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import com.BeatUp.BackEnd.Places.client.KakaoLocalApiClient;
import com.BeatUp.BackEnd.Places.dto.request.NearbySearchRequest;
import com.BeatUp.BackEnd.Places.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaceService {

    private final KakaoLocalApiClient kakaoClient;
    private final CategoryMapper categoryMapper;

    /**
     * 주변 장소 검색 메서드
     */
    public NearbySearchResponse searchNearby(NearbySearchRequest request){
        log.debug("주변 장소 검색 시작 - lat: {}, lng: {}, radius: {}m, categories:{}",
                request.getLat(), request.getLng(), request.getRadius(), request.getCategories());

        // 1. 카테고리 매핑
        List<String> kakaoCodes = categoryMapper.getKakaoCategoryCodes(request.getCategories());
        log.debug("매핑된 카카오 카테고리: {}", kakaoCodes);

        // 2. 카카오맵 API 호출 및 결과 수정
        List<PlaceResponse> allPlaces = new ArrayList<>();

        for(String categoryCode: kakaoCodes){
            try{
                KakaoPlaceSearchResponse kakaoResponse = kakaoClient.searchByCategory(
                        request.getLat(), request.getLng(), request.getRadius(), categoryCode, 15
                );

                if(kakaoResponse != null && kakaoResponse.getDocuments() != null){
                    List<PlaceResponse> categoryPlaces = kakaoResponse.getDocuments().stream()
                            .map(doc -> convertToPlaceResponse(doc, request))
                            .collect(Collectors.toList());

                    allPlaces.addAll(categoryPlaces);
                    log.debug("카테고리 {} 검색 완료 - 결과 {}개", categoryCode,categoryPlaces.size());
                }
            }catch(Exception e){
                log.warn("카테고리 {} 검색 실패: {}개", categoryCode, e.getMessage());
                // 일부 카테고리 실패해도 다른 카테고리 결과는 반환
            }
        }

        // 3. 중복 제거 및 정렬
        List<PlaceResponse> processPlaces = deduplicateAndSort(allPlaces, request);

        // 4. 응답 생성
        NearbySearchResponse response = NearbySearchResponse.builder()
                .places(processPlaces)
                .totalCount(processPlaces.size())
                .searchCenter(LocationResponse.of(request.getLat(), request.getLng()))
                .appliedCategories(request.getCategories())
                .searchRadius(request.getRadius())
                .cacheHit(false)
                .build();

        log.info("주변 장소 검색 완료 - 총 {}개 출력", processPlaces.size());
        return response;
    }

    /**
     * 카카오맵 응답을 PlaceResponse로 반환
     */
    private PlaceResponse convertToPlaceResponse(
            KakaoPlaceSearchResponse.PlaceDocument doc,
            NearbySearchRequest request
    ){
        // 카카오 API가 제공하는 distance 사용(미터 단위)
        int distaceMeters;
        try{
            distaceMeters = Integer.parseInt(doc.getDistance());
        } catch (NumberFormatException e) {
            // distance가 없는 경우 Haversine 공식으로 계산
            distaceMeters = (int) Math.round(caculateDistance(
                request.getLat(), request.getLng(),
                    Double.parseDouble(doc.getY()),  Double.parseDouble(doc.getX())
            ));
        }

        return PlaceResponse.builder()
                .id(doc.getId())
                .name(doc.getPlaceName())
                .address(preferRoadAddress(doc.getRoadAddressName(), doc.getAddressName()))
                .phone(cleanPhoneNumber(doc.getPhone()))
                .category(CategoryResponse.builder()
                        .name(categoryMapper.getUserFriendlyName(doc.getCategoryGroupCode()))
                        .code(doc.getCategoryGroupCode())
                        .build())
                .location(LocationResponse.builder()
                        .lat(Double.parseDouble(doc.getY()))
                        .lng(Double.parseDouble(doc.getX()))
                        .build())
                .distance(DistanceResponse.builder()
                        .meters(distaceMeters)
                        .displayText(formatDistance(distaceMeters))
                        .build())
                .placeUrl(doc.getPlaceUrl())
                .build();
    }

    /**
     * 중복 제거 및 거리순 정렬
     */
    private List<PlaceResponse> deduplicateAndSort(List<PlaceResponse> places, NearbySearchRequest request){
        // 1. 중복 제거(같은 ID의 장소, 더 상세한 정보를 가진 것 선택)
        Map<String, PlaceResponse> uniqueReplaces = places.stream()
                .collect(Collectors.toMap(
                        PlaceResponse::getId,
                        Function.identity(),
                        (existing, replacement) -> {
                            // 전화번호가 있는 것을 우선 선택
                            if(replacement.getPhone() != null && existing.getPhone() == null){
                                return replacement;
                            }
                            return existing;
                        }
                ));

        // 2. 거리순 정렬 후 페이징 적용
        return uniqueReplaces.values().stream()
                .sorted(Comparator.comparing(place -> place.getDistance().getMeters()))
                .skip(request.getOffset())
                .limit(request.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * Haversine 공식을 이용한 거리 계산
     */
    private double caculateDistance(double lat1, double lng1, double lat2, double lng2){
        final double R = 6371000; // 지구 반지름

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) +
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 거리를 사용자 친화적 형태로 포맷팅
     */
    private String formatDistance(int meters){
        if(meters < 1000){
            return String.format("내 위치에서 %dm", meters);
        }else{
            double km = meters / 1000.0;
            if(km < 10){
                return String.format("내 위치에서 %.1fkm", km);
            }else {
                return String.format("내 위치에서 %dkm", (int)Math.round(km));
            }
        }
    }

    /**
     * 도로명 주소 우선 선택
     */
    private String preferRoadAddress(String roadAddress, String address){
        if(roadAddress != null && !roadAddress.trim().isEmpty()){
            return roadAddress.trim();
        }
        return address != null ?  address.trim() : "";
    }

    /**
     * 전화번호 정제
     */
    private String cleanPhoneNumber(String phone){
        if(phone == null || phone.trim().isEmpty()){
            return null;
        }
        return phone.trim().replaceAll("\\s+", " ");
    }
}
