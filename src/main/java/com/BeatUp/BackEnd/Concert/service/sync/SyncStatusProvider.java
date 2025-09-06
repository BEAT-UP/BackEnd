package com.BeatUp.BackEnd.Concert.service.sync;


import com.BeatUp.BackEnd.Concert.enums.DataSource;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncStatusProvider {

    private final ConcertRepository concertRepository;
    private final ConcertService concertService;

    /**
     * 현재 동기화 상태 조회
     */
    public SyncStatus getCurrentStatus(boolean isRunning){
        long totalConcerts = concertRepository.count();
        long kopisConcerts = concertRepository.findByDataSource(DataSource.KOPIS).size();
        long needsSyncCount = concertService.getConcertsNeedingSync().size();

        return SyncStatus.builder()
                .isRunning(isRunning)
                .totalConcerts(totalConcerts)
                .kopisConcerts(kopisConcerts)
                .needsSyncCount(needsSyncCount)
                .kopisDataRatio(totalConcerts > 0 ? (double) kopisConcerts / totalConcerts * 100 : 0)
                .build();
    }
}
