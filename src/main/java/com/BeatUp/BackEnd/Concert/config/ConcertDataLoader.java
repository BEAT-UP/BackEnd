package com.BeatUp.BackEnd.Concert.config;

import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Concert.service.ConcertSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ConcertDataLoader {

    private final ConcertRepository concertRepository;
    private final ConcertSyncService concertSyncService;

    @Bean
    ApplicationRunner loadConcertData(){
        return args -> {
            long initialCount = concertRepository.count();

            if(initialCount == 0){
                log.info("Concert 데이터가 없어 시드 데이터 로딩 시작");
            }

            // 이미 데이터가 있으면 추가하지 않음
            if(concertRepository.count()  > 0){
                return;
            }

            // 시드 데이터 5개 추가
            concertRepository.save(new Concert(
                    "New Jeans FANCON 'Bunnies Camp'",
                    "고척 스카이돔",
                    LocalDateTime.of(2025, 8, 3, 18, 0),
                    "K-POP",
                    "77000원"
            ));

            concertRepository.save(new Concert(
                    "SEVENTEEN WORLD TOUR",
                    "인천 인스파이어 아레나",
                    LocalDateTime.of(2025, 9, 10, 19, 0),
                    "K-POP",
                    "132000원"
            ));

            concertRepository.save(new Concert(
                    "IU CONCERT 'The Golden Hour'",
                    "서울 올림픽체조경기장",
                    LocalDateTime.of(2025, 10, 1, 18, 30),
                    "K-POP",
                    "99000원"
            ));

            concertRepository.save(new Concert(
                    "뮤지컬 레미제라블",
                    "충무아트센터 대극장",
                    LocalDateTime.of(2025, 8, 15, 20, 0),
                    "뮤지컬",
                    "140000원"
            ));

            concertRepository.save(new Concert(
                    "BLACKPINK WORLD TOUR 'BORN PINK'",
                    "고양종합운동장 주경기장",
                    LocalDateTime.of(2025, 8, 17, 18, 0),
                    "K-POP",
                    "154000원"
            ));

            System.out.println("Concert 시드 데이터" + concertRepository.count() + "개 로딩 완료!");
        };
    }
}
