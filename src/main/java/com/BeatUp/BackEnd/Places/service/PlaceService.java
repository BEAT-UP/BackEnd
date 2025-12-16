package com.BeatUp.BackEnd.Places.service;

import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import com.BeatUp.BackEnd.Places.client.KakaoLocalApiClient;
import com.BeatUp.BackEnd.Places.dto.request.NearbySearchRequest;
import com.BeatUp.BackEnd.Places.dto.response.*;
import com.BeatUp.BackEnd.Places.entity.Place;
import com.BeatUp.BackEnd.Places.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaceService {

    private final KakaoLocalApiClient kakaoClient;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PlaceRepository placeRepository;

    private static final Duration CACHE_TTL = Duration.ofHours(1); // 캐시 TTL

    /**
     * 주변 장소 검색 메서드(PostGIS 우선, 부족하면 Kakao API로 보완)
     */
    public NearbySearchResponse searchNearby(NearbySearchRequest request){
        log.debug("주변 장소 검색 시작 - lat: {}, lng: {}, radius: {}m, categories:{}",
                request.getLat(), request.getLng(), request.getRadius(), request.getCategories());


        // 1. 캐시 키 생성
        String cacheKey = request.getCacheKey();

        // 2. 캐시 조회
        NearbySearchResponse cachedResponse = getCachedResponse(cacheKey);
        if(cachedResponse != null){
            log.info("캐시 히트 - key: {}", cacheKey);
            return cachedResponse.withCacheHit(true);
        }

        log.debug("캐시 미스 - API 호출 필요");

        long startTime = System.currentTimeMillis();

        // 3. PostGIS로 DB에서 조회
        Point centerPoint = Place.createPoint(request.getLat(), request.getLng());

        // 3. 카테고리 매핑
        List<String> kakaoCodes = categoryMapper.getKakaoCategoryCodes(request.getCategories());
        log.debug("매핑된 카카오 카테고리: {}", kakaoCodes);

        List<PlaceResponse> placeResponses = new ArrayList<>();

        try{
            List<Object[]> dbResults = placeRepository.findNearbyPlacesWithDistance(
                    centerPoint,
                    request.getRadius(),
                    kakaoCodes.isEmpty() ? null : kakaoCodes.toArray(new String[0]),
                    request.getLimit() * 2,
                    request.getOffset()
            );

            long dbQueryTime = System.currentTimeMillis() - startTime;
            log.info("PostGIS 쿼리 완료 - {}ms, 결과: {}개", dbQueryTime, dbResults.size());

            // DB 결과를 PlaceResponse로 변환
            placeResponses = dbResults.stream()
                    .map(result -> {
                        Place place = (Place) result[0];
                        Double distance = result[1] != null ? (Double) result[1] : null;
                        int distanceMeters = distance != null ? distance.intValue() : 0;
                        return convertToPlaceResponse(place, request, distanceMeters);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("PostGIS 쿼리 실패 - Kakao API로 대체: {}", e.getMessage());
        }

        // 4. 결과가 부족하면 Kakao API로 보완
        if(placeResponses.size() < request.getLimit() / 2){
            log.debug("DB 결과 부족 ({}개) - Kakao API로 보완", placeResponses.size());

            List<PlaceResponse> kakaoPlaces = fetchFromKakaoAPI(request, kakaoCodes);

            // 중복 제거 후 병합
            Set<String> existingIds = placeResponses.stream()
                    .map(PlaceResponse::getId)
                    .collect(Collectors.toSet());

            List<PlaceResponse> newPlaces = kakaoPlaces.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .limit(request.getLimit() - placeResponses.size())
                    .collect(Collectors.toList());

            placeResponses.addAll(newPlaces);

            // DB에 저장
            savePlacesToDB(newPlaces);
        }else{
            log.debug("DB 결과 충분 ({}개) - Kakao API 호출 생략", placeResponses.size());
        }

        // 5. 거리순 정렬 및 페이징
        List<PlaceResponse> sortedPlaces = placeResponses.stream()
                .sorted(Comparator.comparing(p -> p.getDistance().getMeters()))
                .skip(request.getOffset())
                .limit(request.getLimit())
                .collect(Collectors.toList());

        // 6. 응답 생성
        NearbySearchResponse response = NearbySearchResponse.builder()
                .places(sortedPlaces)
                .totalCount(sortedPlaces.size())
                .searchCenter(LocationResponse.of(request.getLat(), request.getLng()))
                .appliedCategories(request.getCategories())
                .searchRadius(request.getRadius())
                .cacheHit(false)
                .build();

        // 7. 캐시 저장
        savedToCache(cacheKey, response);

        log.info("주변 장소 검색 완료 - 총 {}개 출력", sortedPlaces.size());
        return response;
    }

    /**
     * Kakao API에서 장소 조회(기존 로직 분리)
     */
    private List<PlaceResponse> fetchFromKakaoAPI(
            NearbySearchRequest request,
            List<String> kakaoCodes
    ){
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
                }
            } catch (Exception e) {
                log.warn("카테고리 {} 검색 실패: {}", categoryCode, e.getMessage());
            }
        }
        return deduplicateAndSort(allPlaces, request);
    }

    /**
     * 캐시에서 응답 조회
     */
    private NearbySearchResponse getCachedResponse(String cacheKey){
        try{
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if(cached instanceof NearbySearchResponse){
                return (NearbySearchResponse) cached;
            }
        } catch (Exception e) {
            log.warn("캐시 조회 실패 - key: {}, error: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 캐시에 응답 저장
     */
    private void savedToCache(String cacheKey, NearbySearchResponse response){
        try{
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
            log.debug("캐시 저장 완료 - key: {}, TTL: {}", cacheKey, CACHE_TTL.toHours());
        } catch (Exception e) {
            log.warn("캐시 저장 실패 - key: {}, error: {}", cacheKey, e.getMessage());
            // 캐시는 실패해도 응답은 정상 반환
        }
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
     * Place 엔티티를 PlaceResponse로 변환
     */
    private PlaceResponse convertToPlaceResponse(
            Place place,
            NearbySearchRequest request,
            int distanceMeters
    ){
        return PlaceResponse.builder()
                .id(place.getKakaoPlaceId())
                .name(place.getName())
                .address(preferRoadAddress(place.getRoadAddress(), place.getAddress()))
                .phone(cleanPhoneNumber(place.getPhone()))
                .category(CategoryResponse.builder()
                        .name(place.getCategoryName() != null ? place.getCategoryName() : 
                                categoryMapper.getUserFriendlyName(place.getCategoryCode()))
                        .code(place.getCategoryCode())
                        .build())
                .location(LocationResponse.builder()
                        .lat(place.getLocation().getY())
                        .lng(place.getLocation().getX())
                        .build())
                .distance(DistanceResponse.builder()
                        .meters(distanceMeters)
                        .displayText(formatDistance(distanceMeters))
                        .build())
                .placeUrl(place.getPlaceUrl())
                .build();
    }

    /**
     * Place 엔티티를 DB에 저장
     */
    @Transactional
    public void savePlacesToDB(List<PlaceResponse> placeResponses) {
        for (PlaceResponse pr : placeResponses) {
            try {
                placeRepository.findByKakaoPlaceId(pr.getId())
                        .ifPresentOrElse(
                                existing -> {
                                    // 기존 데이터 업데이트
                                    existing.setName(pr.getName());
                                    existing.setAddress(pr.getAddress());
                                    existing.setPhone(pr.getPhone());
                                    existing.setLastSyncedAt(LocalDateTime.now());
                                    placeRepository.save(existing);
                                },
                                () -> {
                                    // 새 데이터 저장
                                    Place newPlace = Place.builder()
                                            .kakaoPlaceId(pr.getId())
                                            .name(pr.getName())
                                            .address(pr.getAddress())
                                            .roadAddress(pr.getAddress())
                                            .phone(pr.getPhone())
                                            .categoryCode(pr.getCategory().getCode())
                                            .categoryName(pr.getCategory().getName())
                                            .location(Place.createPoint(
                                                    pr.getLocation().getLat(),
                                                    pr.getLocation().getLng()
                                            ))
                                            .placeUrl(pr.getPlaceUrl())
                                            .lastSyncedAt(LocalDateTime.now())
                                            .build();
                                    placeRepository.save(newPlace);
                                }
                        );
            } catch (Exception e) {
                log.warn("장소 저장 실패 - id: {}, error: {}", pr.getId(), e.getMessage());
            }
        }
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

    /**
     * 공연장명으로 좌표 조회
     */
    public LocationResponse searchVenueCoordinates(String venueName) {
        if (venueName == null || venueName.trim().isEmpty()) {
            log.warn("공연장명이 비어있습니다.");
            return null;
        }

        try {
            log.debug("공연장 좌표 조회 시작 - venue: {}", venueName);
            
            // 카카오 Places API로 키워드 검색
            KakaoPlaceSearchResponse response = kakaoClient.searchByKeyword(venueName, 1);
            
            if (response != null && response.getDocuments() != null && !response.getDocuments().isEmpty()) {
                KakaoPlaceSearchResponse.PlaceDocument place = response.getDocuments().get(0);
                
                double lat = Double.parseDouble(place.getY());
                double lng = Double.parseDouble(place.getX());
                
                log.info("공연장 좌표 조회 성공 - venue: {}, lat: {}, lng: {}", venueName, lat, lng);
                
                return LocationResponse.builder()
                        .lat(lat)
                        .lng(lng)
                        .build();
            } else {
                log.warn("공연장 좌표를 찾을 수 없습니다 - venue: {}", venueName);
                return null;
            }
        } catch (Exception e) {
            log.error("공연장 좌표 조회 실패 - venue: {}, error: {}", venueName, e.getMessage());
            return null;
        }
    }
}
