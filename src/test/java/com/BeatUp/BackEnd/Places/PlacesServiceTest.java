package com.BeatUp.BackEnd.Places;


import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import com.BeatUp.BackEnd.Places.client.KakaoLocalApiClient;
import com.BeatUp.BackEnd.Places.dto.request.NearbySearchRequest;
import com.BeatUp.BackEnd.Places.dto.response.KakaoPlaceSearchResponse;
import com.BeatUp.BackEnd.Places.dto.response.NearbySearchResponse;
import com.BeatUp.BackEnd.Places.dto.response.PlaceResponse;
import com.BeatUp.BackEnd.Places.repository.PlaceRepository;
import com.BeatUp.BackEnd.Places.service.PlaceService;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlacesServiceTest {

    @Mock
    private KakaoLocalApiClient kakaoClient;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private MonitoringUtil monitoringUtil;

    private PlaceService placeService;

    @BeforeEach
    void setUp(){
        // RedisTemplate Mock 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 캐시 조회 시 null 반환 (캐시 미스 시뮬레이션)
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // MonitoringUtil 메서드 모킹
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Timer.Sample mockSample = Timer.start(meterRegistry);
        when(monitoringUtil.startApiCallTimer(anyString())).thenReturn(mockSample);
        doNothing().when(monitoringUtil).recordApiCall(any(Timer.Sample.class), anyString(), anyString());
        doNothing().when(monitoringUtil).recordApiCall(anyString(), anyString());
        
        // PlaceService 생성자에 Mock 객체들을 직접 주입
        // PlaceService(KakaoLocalApiClient kakaoClient, CategoryMapper categoryMapper, RedisTemplate redisTemplate, PlaceRepository placeRepository, MonitoringUtil monitoringUtil) 순서
        placeService = new PlaceService(kakaoClient, categoryMapper, redisTemplate, placeRepository, monitoringUtil);
    }

    @Test
    @DisplayName("주변 장소 검색 성공")
    void searchNearby_Success(){
        // Given
        NearbySearchRequest request = NearbySearchRequest.builder()
                .lat(37.5665)
                .lng(126.9780)
                .radius(100)
                .categories(Arrays.asList("음식점", "카페"))
                .limit(10)
                .offset(0)
                .build();

        // CategoryMapper Mock 설정
        when(categoryMapper.getKakaoCategoryCodes(request.getCategories()))
                .thenReturn(Arrays.asList("FD6", "CE7"));

        // 카카오 API 응답 모킹
        KakaoPlaceSearchResponse mockResponse = createMockResponse();
        when(kakaoClient.searchByCategory(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
                .thenReturn(mockResponse);

        // When
        NearbySearchResponse result = placeService.searchNearby(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlaces()).isNotEmpty();
        assertThat(result.getTotalCount()).isGreaterThan(0);
        assertThat(result.getSearchCenter().getLat()).isEqualTo(37.5665);
        assertThat(result.getSearchCenter().getLng()).isEqualTo(126.9780);

        // 카카오 API가 각 카테고리별로 호출되었는지 확인
        verify(kakaoClient, times(2)).searchByCategory(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt());
        verify(categoryMapper).getKakaoCategoryCodes(request.getCategories());
    }

    @Test
    @DisplayName("중복 제거 및 거리순 정렬 확인")
    void searchNearby_DeduplicationAndSorting(){

        // Given
        NearbySearchRequest request = NearbySearchRequest.builder()
                .lat(37.5665)
                .lng(126.9780)
                .categories(Arrays.asList("카페"))
                .limit(10)
                .build();

        when(categoryMapper.getKakaoCategoryCodes(request.getCategories()))
                .thenReturn(Arrays.asList("CE7"));
        when(categoryMapper.getUserFriendlyName("CE7")).thenReturn("카페");

        // 거리가 다른 두 장소와 중복 장소 생성
        KakaoPlaceSearchResponse mockResponse = createMockResponseWithDuplication();
        when(kakaoClient.searchByCategory(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
                .thenReturn(mockResponse);

        // When
        NearbySearchResponse result = placeService.searchNearby(request);

        // Then
        assertThat(result.getPlaces()).hasSize(2); // 중복 제거로 2개만
        // 거리순 정렬 확인(가까운 순)
        assertThat(result.getPlaces().get(0).getDistance().getMeters())
                .isLessThan(result.getPlaces().get(1).getDistance().getMeters());
    }


    @Test
    @DisplayName("거리 포맷팅 정확성 확인")
    void searchNearby_DistanceFormatting(){
        // Given
        NearbySearchRequest request = NearbySearchRequest.builder()
                .lat(37.5665)
                .lng(126.9780)
                .categories(Arrays.asList("카페"))
                .build();

        when(categoryMapper.getKakaoCategoryCodes(request.getCategories()))
                .thenReturn(Arrays.asList("CE7"));
        when(categoryMapper.getUserFriendlyName("CE7")).thenReturn("카페");

        KakaoPlaceSearchResponse mockResponse = createMockResponseWithVariousDistances();
        when(kakaoClient.searchByCategory(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
                .thenReturn(mockResponse);

        // When
        NearbySearchResponse result = placeService.searchNearby(request);

        // Then
        List<PlaceResponse> places = result.getPlaces();
        assertThat(places.get(0).getDistance().getDisplayText()).contains("내 위치에서").contains("m");
        assertThat(places.get(1).getDistance().getDisplayText()).contains("내 위치에서").contains("km");

    }

    private KakaoPlaceSearchResponse createMockResponse(){
        KakaoPlaceSearchResponse.PlaceDocument doc = new KakaoPlaceSearchResponse.PlaceDocument();
        doc.setId("12345");
        doc.setPlaceName("테스트 카페");
        doc.setCategoryGroupCode("CE7");
        doc.setPhone("02-1234-5678");
        doc.setAddressName("서울 강남구 테스트동 123");
        doc.setRoadAddressName("서울 강남구 테스트로 123");
        doc.setX("126.9780");
        doc.setY("37.5665");
        doc.setDistance("100");
        doc.setPlaceUrl("http://place.map.kakao.com/12345");

        KakaoPlaceSearchResponse.Meta meta = new KakaoPlaceSearchResponse.Meta();
        meta.setTotalCount(1);

        return KakaoPlaceSearchResponse.builder()
                .meta(meta)
                .documents(Arrays.asList(doc))
                .build();
    }

    private KakaoPlaceSearchResponse createMockResponseWithDuplication(){
        KakaoPlaceSearchResponse.PlaceDocument doc1 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc1.setId("1");
        doc1.setPlaceName("가까운 카페");
        doc1.setCategoryGroupCode("CE7");
        doc1.setDistance("100");
        doc1.setY("37.5665");
        doc1.setX("126.9780");
        doc1.setAddressName("주소1");
        doc1.setPlaceUrl("http://place.map.kakao.com/1");

        KakaoPlaceSearchResponse.PlaceDocument doc2 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc2.setId("2");
        doc2.setPlaceName("먼 카페");
        doc2.setCategoryGroupCode("CE7");
        doc2.setDistance("500");
        doc2.setY("37.5670");
        doc2.setX("126.9785");
        doc2.setAddressName("주소2");
        doc2.setPlaceUrl("http://place.map.kakao.com/2");

        // 중복(같은 ID)
        KakaoPlaceSearchResponse.PlaceDocument doc3 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc3.setId("1");
        doc3.setPlaceName("중복 카페");
        doc3.setCategoryGroupCode("CE7");
        doc3.setDistance("105");
        doc3.setY("37.5665");
        doc3.setX("126.9780");
        doc3.setAddressName("주소1");
        doc3.setPlaceUrl("http://place.map.kakao.com/1");

        return KakaoPlaceSearchResponse.builder()
                .documents(Arrays.asList(doc1, doc2, doc3))
                .meta(new KakaoPlaceSearchResponse.Meta())
                .build();
    }

    private KakaoPlaceSearchResponse createMockResponseWithVariousDistances() {
        KakaoPlaceSearchResponse.PlaceDocument doc1 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc1.setId("1");
        doc1.setPlaceName("가까운 장소");
        doc1.setCategoryGroupCode("CE7");
        doc1.setDistance("300"); // 300m
        doc1.setY("37.5665");
        doc1.setX("126.9780");
        doc1.setAddressName("주소1");
        doc1.setPlaceUrl("http://place.map.kakao.com/1");

        KakaoPlaceSearchResponse.PlaceDocument doc2 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc2.setId("2");
        doc2.setPlaceName("먼 장소");
        doc2.setCategoryGroupCode("CE7");
        doc2.setDistance("1500"); // 1.5km
        doc2.setY("37.5670");
        doc2.setX("126.9785");
        doc2.setAddressName("주소2");
        doc2.setPlaceUrl("http://place.map.kakao.com/2");

        return KakaoPlaceSearchResponse.builder()
                .documents(Arrays.asList(doc1, doc2))
                .meta(new KakaoPlaceSearchResponse.Meta())
                .build();
    }

}
