package com.BeatUp.BackEnd;

import com.BeatUp.BackEnd.Places.client.KakaoLocalApiClient;
import com.BeatUp.BackEnd.Places.config.KakaoApiConfig;
import com.BeatUp.BackEnd.Places.dto.response.KakaoPlaceSearchResponse;
import com.BeatUp.BackEnd.Places.exception.KakaoApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "kakao.api.base-url=http://localhost:8089",
        "kakao.api.key=test-api-key"
})
public class KakaoMapApiTest {

    private WireMockServer wireMockServer;
    private KakaoLocalApiClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        KakaoApiConfig config = new KakaoApiConfig();
        config.setKey("test-api-key");
        config.setBaseUrl("http://localhost:8089");

        client = new KakaoLocalApiClient(config.kakaoWebClient());
    }

    @AfterEach
    void tearDown(){
        wireMockServer.stop();
    }

    @Test
    @DisplayName("카테고리 검색 성공 테스트")
    void searchByCategory_Success()throws Exception{
        // Given
        KakaoPlaceSearchResponse mockResponse = createMockResponse();

        stubFor(get(urlPathEqualTo("/v2/local/search/category.json"))
                .withQueryParam("category_group_code", equalTo("FD6"))
                .withQueryParam("x", equalTo("126.9780"))
                .withQueryParam("y", equalTo("37.5665"))
                .withHeader("Authorization", equalTo("KakaoAK test-api-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // When
        KakaoPlaceSearchResponse result = client.searchByCategory(
                37.5665, 126.9780, 1000,"FD6", 10
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocuments()).hasSize(2);
        assertThat(result.getDocuments().get(0).getPlaceName()).isEqualTo("테스트 음식점");

        verify(getRequestedFor(urlPathEqualTo("v2/local/search/category.json"))
                .withQueryParam("category_group_code", equalTo("FD6")));
    }

    @Test
    @DisplayName("API 호출 제한 초과 시 예외 처리")
    void searchByCategory_RateLimitExceeded(){
        // Given
        stubFor(get(urlPathEqualTo("/v2/local/search/category.json"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("{\"errorType\":\"QueryRequestException\",\"message\":\"호출량이 초과되었습니다.\"}")));

        // When & Then
        assertThatThrownBy(() -> client.searchByCategory(37.5665, 126.9780, 1000, "FD6", 10))
                .isInstanceOf(KakaoApiException.class)
                .hasMessageContaining("호출 제한을 초과");
    }

    private KakaoPlaceSearchResponse createMockResponse(){
        KakaoPlaceSearchResponse.PlaceDocument doc1 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc1.setId("12345");
        doc1.setPlaceName("테스트 음식점");
        doc1.setCategoryGroupName("음식점 > 한식");
        doc1.setCategoryGroupCode("FD6");
        doc1.setPhone("02-1234-5678");
        doc1.setAddressName("서울 강남구 테스트동 123");
        doc1.setX("126.9780");
        doc1.setY("37.5665");
        doc1.setPlaceUrl("http://place.map.kakao.com/12345");
        doc1.setDistance("100");

        KakaoPlaceSearchResponse.PlaceDocument doc2 = new KakaoPlaceSearchResponse.PlaceDocument();
        doc2.setId("67890");
        doc2.setPlaceName("테스트 카페");
        doc2.setCategoryGroupName("음식점 > 카페");
        doc2.setCategoryGroupCode("CE7");

        KakaoPlaceSearchResponse.Meta meta = new KakaoPlaceSearchResponse.Meta();
        meta.setTotalCount(2);
        meta.setPageableCount(2);
        meta.setIsEnd(true);

        return KakaoPlaceSearchResponse.builder()
                .meta(meta)
                .documents(List.of(doc1, doc2))
                .build();
    }

}
