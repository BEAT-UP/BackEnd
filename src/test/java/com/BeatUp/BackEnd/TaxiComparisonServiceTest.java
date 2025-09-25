package com.BeatUp.BackEnd;


import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Match.entity.MatchGroup;
import com.BeatUp.BackEnd.Match.repository.MatchGroupRepository;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import com.BeatUp.BackEnd.Match.taxi.service.TaxiComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaxiComparisonServiceTest {

    @Mock
    private MatchGroupRepository matchGroupRepository;

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private TaxiComparisonService taxiComparisonService;

    private MatchGroup matchGroup;
    private Concert concert;

    @BeforeEach
    void setUp(){
        // 매칭 그룹 설정
        matchGroup = new MatchGroup();
        matchGroup.setId(UUID.randomUUID());
        matchGroup.setConcertId(UUID.randomUUID());
        matchGroup.setDestBucket("37.5665, 126.9780"); // 서울 중심가

        // 공연장 설정
        concert = new Concert();
        concert.setId(matchGroup.getConcertId());
        concert.setVenue("올림픽공원");
        concert.setVenueLat(37.5219);
        concert.setVenueLng(127.1282);
    }

    @Test()
    @DisplayName("택시 서비스 비교 - 정상 케이스")
    void compareService_NormalCase_ReturnsAllServices(){
        // Given
        when(matchGroupRepository.findById(any(UUID.class))).thenReturn(Optional.of(matchGroup));
        when(concertRepository.findById(any(UUID.class))).thenReturn(Optional.of(concert));

        // When
        List<TaxiServiceResponse> responses = taxiComparisonService.compareService(matchGroup.getId());

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size()); // 카카오T, 우버, 타다

        // 서비스명 확인
        List<String> serviceNames = responses.stream()
                .map(TaxiServiceResponse::getServiceName)
                .toList();
        assertTrue(serviceNames.contains("카카오T"));
        assertTrue(serviceNames.contains("우버"));
        assertTrue(serviceNames.contains("타다"));
    }

    @Test
    @DisplayName("택시 서비스 비교 - 매칭 그룹 없음")
    void compareService_MatchGroupNotFound_ThrowsException(){
        // Given
        when(matchGroupRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            taxiComparisonService.compareService(UUID.randomUUID());
        });
    }

    @Test
    @DisplayName("택시 서비스 비교 - 공연장 없음")
    void compareService_ConcertNotFound_ThrowsException(){
        // Given
        when(matchGroupRepository.findById(any(UUID.class))).thenReturn(Optional.of(matchGroup));
        when(concertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            taxiComparisonService.compareService(matchGroup.getId());
        });
    }

    @Test
    @DisplayName("카카오T 요금 계산 - 기본 거리")
    void getKakaoTPrice_ShortDistance_ReturnBaseFare(){
        // Given
        double pickupLat = 37.5665;
        double pickupLng = 126.9780;
        double destLat = 37.5700;
        double destLng = 126.9800; // 약 1km 거리

        // When
        TaxiServiceResponse response = taxiComparisonService.getKakaoTPrice(pickupLat, pickupLng, destLat, destLng);

        // Then
        assertNotNull(response);
        assertEquals("카카오T", response.getServiceName());
        assertEquals(3900, response.getEstimatePrice()); // 기본요금 + 시간요금
        assertTrue(response.getEstimatedTime() > 0);
    }

    @Test
    @DisplayName("우버 요금 계산 - 장거리")
    void getUberPrice_LongDistance_ReturnsCorrectFare() {
        // Given
        double pickupLat = 37.5665;
        double pickupLng = 126.9780;
        double destLat = 37.5000;
        double destLng = 127.0000; // 약 10km 거리

        // When
        TaxiServiceResponse response = taxiComparisonService.getUberPrice(pickupLat, pickupLng, destLat, destLng);

        // Then
        assertNotNull(response);
        assertEquals("우버", response.getServiceName());
        assertTrue(response.getEstimatePrice() >= 3500); // 최소요금
        assertTrue(response.getEstimatedTime() > 0);
    }

    @Test
    @DisplayName("타다 요금 계산 - 기본 거리")
    void getTadaPrice_ShortDistance_ReturnsBaseFare() {
        // Given
        double pickupLat = 37.5665;
        double pickupLng = 126.9780;
        double destLat = 37.5700;
        double destLng = 126.9800; // 약 1km 거리

        // When
        TaxiServiceResponse response = taxiComparisonService.getTadaPrice(pickupLat, pickupLng, destLat, destLng);

        // Then
        assertNotNull(response);
        assertEquals("타다", response.getServiceName());
        assertEquals(5000, response.getEstimatePrice()); // 최소요금
        assertTrue(response.getEstimatedTime() > 0);
    }

    @Test
    @DisplayName("메시지 포맷팅 - 정상 케이스")
    void formatTaxiMessage_NormalCase_ReturnsFormattedMessage() {
        // Given
        List<TaxiServiceResponse> options = new ArrayList<>();
        options.add(new TaxiServiceResponse("카카오T", 8000, 15));
        options.add(new TaxiServiceResponse("우버", 7500, 12));
        options.add(new TaxiServiceResponse("타다", 9000, 18));

        // When
        String message = taxiComparisonService.formatTaxiMessage(options);

        // Then
        assertNotNull(message);
        assertTrue(message.contains("택시 서비스 가격 비교"));
        assertTrue(message.contains("카카오T"));
        assertTrue(message.contains("우버"));
        assertTrue(message.contains("타다"));
        assertTrue(message.contains("8,000원"));
        assertTrue(message.contains("7,500원"));
        assertTrue(message.contains("9,000원"));
    }

    @Test
    @DisplayName("메시지 포맷팅 - 빈 리스트")
    void formatTaxiMessage_EmptyList_ReturnsErrorMessage() {
        // Given
        List<TaxiServiceResponse> options = List.of();

        // When
        String message = taxiComparisonService.formatTaxiMessage(options);

        // Then
        assertNotNull(message);
        assertTrue(message.contains("택시 서비스 정보를 가져올 수 없습니다"));
    }
}
