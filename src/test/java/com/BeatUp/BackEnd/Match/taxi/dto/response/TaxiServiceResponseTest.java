package com.BeatUp.BackEnd.Match.taxi.dto.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TaxiServiceResponseTest {

    @Test
    @DisplayName("기본 생성자 테스트")
    void constructor_Default_ReturnsEmptyObject() {
        // When
        TaxiServiceResponse response = new TaxiServiceResponse();

        // Then
        assertNull(response.getServiceName());
        assertNull(response.getEstimatePrice());
        assertNull(response.getEstimatedTime());
        assertNull(response.getDeeplink());
        assertNull(response.getWeblink());
        assertNull(response.getDiscounts());
    }

    @Test
    @DisplayName("필수 필드 생성자 테스트")
    void constructor_RequiredFields_ReturnsCorrectObject() {
        // Given
        String serviceName = "카카오T";
        Integer estimatePrice = 8000;
        Integer estimatedTime = 15;

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(serviceName, estimatePrice, estimatedTime);

        // Then
        assertEquals(serviceName, response.getServiceName());
        assertEquals(estimatePrice, response.getEstimatePrice());
        assertEquals(estimatedTime, response.getEstimatedTime());
        assertNull(response.getDeeplink());
        assertNull(response.getWeblink());
        assertNull(response.getDiscounts());
    }

    @Test
    @DisplayName("전체 필드 생성자 테스트")
    void constructor_AllFields_ReturnsCorrectObject() {
        // Given
        String serviceName = "우버";
        Integer estimatePrice = 7500;
        Integer estimatedTime = 12;
        String deeplink = "uber://ride";
        String weblink = "https://uber.com";
        List<String> discounts = List.of("첫 이용 할인", "주말 할인");

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(
                serviceName, estimatePrice, estimatedTime, deeplink, weblink, discounts
        );

        // Then
        assertEquals(serviceName, response.getServiceName());
        assertEquals(estimatePrice, response.getEstimatePrice());
        assertEquals(estimatedTime, response.getEstimatedTime());
        assertEquals(deeplink, response.getDeeplink());
        assertEquals(weblink, response.getWeblink());
        assertEquals(discounts, response.getDiscounts());
    }

    @Test
    @DisplayName("Setter 테스트")
    void setters_AllFields_UpdatesCorrectly() {
        // Given
        TaxiServiceResponse response = new TaxiServiceResponse();
        String serviceName = "타다";
        Integer estimatePrice = 9000;
        Integer estimatedTime = 18;
        String deeplink = "tada://ride";
        String weblink = "https://tada.com";
        List<String> discounts = List.of("신규 회원 할인");

        // When
        response.setServiceName(serviceName);
        response.setEstimatePrice(estimatePrice);
        response.setEstimatedTime(estimatedTime);
        response.setDeeplink(deeplink);
        response.setWeblink(weblink);
        response.setDiscounts(discounts);

        // Then
        assertEquals(serviceName, response.getServiceName());
        assertEquals(estimatePrice, response.getEstimatePrice());
        assertEquals(estimatedTime, response.getEstimatedTime());
        assertEquals(deeplink, response.getDeeplink());
        assertEquals(weblink, response.getWeblink());
        assertEquals(discounts, response.getDiscounts());
    }

    @Test
    @DisplayName("카카오T 응답 생성 테스트")
    void createKakaoTResponse_ReturnsCorrectValues() {
        // Given
        String serviceName = "카카오T";
        Integer estimatePrice = 8000;
        Integer estimatedTime = 15;

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(serviceName, estimatePrice, estimatedTime);

        // Then
        assertEquals("카카오T", response.getServiceName());
        assertEquals(8000, response.getEstimatePrice());
        assertEquals(15, response.getEstimatedTime());
    }

    @Test
    @DisplayName("우버 응답 생성 테스트")
    void createUberResponse_ReturnsCorrectValues() {
        // Given
        String serviceName = "우버";
        Integer estimatePrice = 7500;
        Integer estimatedTime = 12;

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(serviceName, estimatePrice, estimatedTime);

        // Then
        assertEquals("우버", response.getServiceName());
        assertEquals(7500, response.getEstimatePrice());
        assertEquals(12, response.getEstimatedTime());
    }

    @Test
    @DisplayName("타다 응답 생성 테스트")
    void createTadaResponse_ReturnsCorrectValues() {
        // Given
        String serviceName = "타다";
        Integer estimatePrice = 9000;
        Integer estimatedTime = 18;

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(serviceName, estimatePrice, estimatedTime);

        // Then
        assertEquals("타다", response.getServiceName());
        assertEquals(9000, response.getEstimatePrice());
        assertEquals(18, response.getEstimatedTime());
    }

    @Test
    @DisplayName("할인 정보가 있는 응답 테스트")
    void createResponseWithDiscounts_ReturnsCorrectValues() {
        // Given
        String serviceName = "카카오T";
        Integer estimatePrice = 8000;
        Integer estimatedTime = 15;
        String deeplink = "kakao://taxi";
        String weblink = "https://taxi.kakao.com";
        List<String> discounts = List.of("첫 이용 할인 20%", "주말 할인 10%");

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(
                serviceName, estimatePrice, estimatedTime, deeplink, weblink, discounts
        );

        // Then
        assertEquals("카카오T", response.getServiceName());
        assertEquals(8000, response.getEstimatePrice());
        assertEquals(15, response.getEstimatedTime());
        assertEquals("kakao://taxi", response.getDeeplink());
        assertEquals("https://taxi.kakao.com", response.getWeblink());
        assertEquals(2, response.getDiscounts().size());
        assertTrue(response.getDiscounts().contains("첫 이용 할인 20%"));
        assertTrue(response.getDiscounts().contains("주말 할인 10%"));
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void handleNullValues_WorksCorrectly() {
        // Given
        TaxiServiceResponse response = new TaxiServiceResponse();

        // When
        response.setServiceName(null);
        response.setEstimatePrice(null);
        response.setEstimatedTime(null);
        response.setDeeplink(null);
        response.setWeblink(null);
        response.setDiscounts(null);

        // Then
        assertNull(response.getServiceName());
        assertNull(response.getEstimatePrice());
        assertNull(response.getEstimatedTime());
        assertNull(response.getDeeplink());
        assertNull(response.getWeblink());
        assertNull(response.getDiscounts());
    }

    @Test
    @DisplayName("빈 할인 리스트 테스트")
    void createResponseWithEmptyDiscounts_ReturnsCorrectValues() {
        // Given
        String serviceName = "우버";
        Integer estimatePrice = 7500;
        Integer estimatedTime = 12;
        List<String> emptyDiscounts = List.of();

        // When
        TaxiServiceResponse response = new TaxiServiceResponse(serviceName, estimatePrice, estimatedTime);
        response.setDiscounts(emptyDiscounts);

        // Then
        assertEquals("우버", response.getServiceName());
        assertEquals(7500, response.getEstimatePrice());
        assertEquals(12, response.getEstimatedTime());
        assertNotNull(response.getDiscounts());
        assertTrue(response.getDiscounts().isEmpty());
    }
}
