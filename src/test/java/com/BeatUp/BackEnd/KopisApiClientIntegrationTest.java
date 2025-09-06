package com.BeatUp.BackEnd;


import com.BeatUp.BackEnd.Concert.client.KopisApiClient;
import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.enums.KopisGenre;
import com.BeatUp.BackEnd.Concert.enums.KopisPerformanceState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class KopisApiClientIntegrationTest {

    @Autowired
    private KopisApiClient kopisApiClient;

    @Test
    @DisplayName("실제 KOPIS API 공연 목록 조회 테스트")
    void searchPerformances_RealApi_Success(){
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);

        // When - 뮤지컬 검색
        List<KopisPerformanceDto> musicals = kopisApiClient.searchPerformances(
                startDate, endDate, KopisGenre.MUSICAL, KopisPerformanceState.ONGOING, null, 1, 10
        );

        // Then
        log.info("조회한 뮤지컬 수 : {}", musicals.size());

        if(!musicals.isEmpty()){
            KopisPerformanceDto first = musicals.get(0);
            log.info("첫 번째 뮤지컬: {} (ID: {}, 장소: {}, 상태: {}",
                    first.getPrfnm(), first.getMt20id(), first.getFcltynm(), first.getPrfstate());

            assertThat(first.getMt20id()).isNotBlank();
            assertThat(first.getPrfnm()).isNotBlank();
            assertThat(first.getGenrenm()).contains("뮤지컬");
        }

        assertThat(musicals).isNotNull();
    }

    @Test
    @DisplayName("실제 KOPIS API 공연 상세 조회 테스트")
    void getPerformanceDetail_RealApi_Success(){
        // Given - 현재 목록에서 실제 공연 ID를 가져옴
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);

        List<KopisPerformanceDto> performances = kopisApiClient.searchPerformances(
                startDate, endDate, null, null, null, 1, 5
        );

        if(performances.isEmpty()){
            log.warn("테스트할 공연이 없습니다");
            return;
        }

        String testId = performances.get(0).getMt20id();
        log.info("테스트할 공연 ID: {}", testId);

        // When
        Optional<KopisPerformanceDto> result = kopisApiClient.getPerformanceDetail(testId);

        // Then
        assertThat(result).isPresent();

        KopisPerformanceDto detail = result.get();
        log.info("상세 정보 - 공연명: {}, 출연진: {}, 런타임: {}, 가격: {}",
                detail.getPrfnm(), detail.getPrfcast(), detail.getPrfruntime(), detail.getPcseguidance());

        assertThat(detail.getMt20id()).isEqualTo(testId);
        assertThat(detail.getPrfnm()).isNotBlank();

    }

    @Test
    @DisplayName("API 제약사항 검증 테스트")
    void apiConstraints_Validation(){
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(50); // 31일 초과

        // When - 31일 초과 요청 시 자동 조정되는지 확인
        List<KopisPerformanceDto> result = kopisApiClient.searchPerformances(
                startDate, endDate, null, null, null, 1, 50
        ); // 100건 초과

        // Then - 오류 없이 결과 반환되어야 함(내부에서 제약사항 처리)
        assertThat(result).isNotNull();
        log.info("제약사항 검증 테스트 완료 - 결과 수 : {}", result.size());
    }
}
