package com.BeatUp.BackEnd.Concert.config;


import com.BeatUp.BackEnd.Concert.client.KopisApiClient;
import com.BeatUp.BackEnd.Concert.enums.KopisGenre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KopisSmokeTestRunner {

    private final KopisApiClient kopisApiClient;

    @Bean
    @Profile("test")
    ApplicationRunner kopisSmokeTest(){
        return args -> {
            try{
                log.info("KOPIS API 연동 테스트 시작");

                var performances = kopisApiClient.searchPerformances(
                        LocalDate.now(),
                        LocalDate.now().plusWeeks(2),
                        KopisGenre.MUSICAL,
                        1
                );

                log.info("KOPIS API 연동 성공 - 조회된 뮤지컬 수: {} ", performances.size());

                if(!performances.isEmpty()){
                    var first = performances.get(0);
                    log.info("샘플 뮤지컬: {} (ID: {},  장소: {})", first.getPrfnm(), first.getMt20id(), first.getFcltynm());

                    var detail = kopisApiClient.getPerformanceDetail(first.getMt20id());
                    if(detail.isPresent()){
                        log.info("상세 조회 성공 - 출연진: {}, 런타임: {}", detail.get().getPrfcast(), detail.get().getPrfruntime());
                    }
                }

                log.info("KOPIS API 연동 테스트 완료");
            } catch (Exception e) {
                log.warn("KOPIS API 연동 테스트 실패(서비스 키 확인 필요): {}", e.getMessage());
            }
        };
    }
}
