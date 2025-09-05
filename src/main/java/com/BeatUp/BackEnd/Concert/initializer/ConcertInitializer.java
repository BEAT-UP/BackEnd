package com.BeatUp.BackEnd.Concert.initializer;

import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.enums.DataSource;

import java.time.LocalDateTime;

public class ConcertInitializer {

    public static void initializeNewFields(Concert concert, LocalDateTime startAt){
        concert.setStartDate(startAt.toLocalDate());
        concert.setEndDate(startAt.toLocalDate());
        concert.setState("공연중");
        concert.setDataSource(DataSource.SEED);
        concert.setLastSyncedAt(LocalDateTime.now());

        // Boolean 기본 필드값
        concert.setIsOpenRun(false);
        concert.setIsChildPerformance(false);
        concert.setIsDaehakro(false);
        concert.setIsFestival(false);
        concert.setIsMusicalLicense(false);
        concert.setIsMusicalCreate(false);
        concert.setIsVisit(false);
    }
}
