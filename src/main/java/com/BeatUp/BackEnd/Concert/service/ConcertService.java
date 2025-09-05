package com.BeatUp.BackEnd.Concert.service;


import com.BeatUp.BackEnd.Concert.dto.ConcertSearchCondition;
import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;

    // 기존 메서드
    public List<Concert> getConcerts(String query, LocalDate date){
        return concertRepository.findByQueryAndDate(query, date);
    }

    // LocalDateTime 버전 호환성 유지
    public List<Concert> getConcerts(String query, LocalDateTime dateTime){
        LocalDate date = dateTime != null ? dateTime.toLocalDate() : null;
        return getConcerts(query, date);
    }

    public Optional<Concert> getConcertById(UUID id){
        return concertRepository.findById(id);
    }

    // 새로운 고급 검색 메서드
    public List<Concert> getConcerts(ConcertSearchCondition condition){
        return concertRepository.searchConcerts(condition);
    }

    public Page<Concert> searchConcertsWithPaging(ConcertSearchCondition condition, Pageable pageable){
        return concertRepository.searchConcerts(condition, pageable);
    }

    // 편의 메서드
    public List<Concert> getConcerts(String query, LocalDate date, String genre, String state, String area){
        ConcertSearchCondition condition = ConcertSearchCondition.builder()
                .query(query)
                .date(date)
                .genre(genre)
                .state(state)
                .area(area)
                .build();

        return getConcerts(condition);
    }

    // KOPIS 연동 메서드
    public Optional<Concert> getConcertByKopisId(String kopisId){
        return concertRepository.findByKopisId(kopisId);
    }

    /**
     * KOPIS DTO를 기반으로 Concert 엔티티를 생성하거나 업데이트(Upsert)
     */
    @Transactional
    public Concert upsertFromKopis(KopisPerformanceDto dto){
        if(dto.getMt20id() == null || dto.getMt20id().trim().isEmpty()){
            log.warn("KOPIS ID가 없는 DTO입니다: {}", dto.getPrfnm());
            return null;
        }

        return concertRepository.findByKopisId(dto.getMt20id())
                .map(existing -> {
                    log.debug("기존 Concert 업데이트: {}", dto.getMt20id());
                    existing.updateFromKopisData(dto);
                    return concertRepository.save(existing);
                })
                .orElseGet(() -> {
                    log.debug("새 Concert 생성: {}", dto.getMt20id());
                    Concert newConcert = Concert.fromKopisData(dto);
                    return concertRepository.save(newConcert);
                });
    }

    /**
     * 여러 KOPIS DTO를 일괄 처리(배치 Upsert)
     */
    @Transactional
    public List<Concert> batchUpsertFromKopis(List<KopisPerformanceDto> dtos){
        return dtos.stream()
                .map(this::upsertFromKopis)
                .filter(concert -> concert != null)
                .toList();
    }

    /**
     * 동기화가 필요한 KOPIS 공연 목록 조회
     */
    public List<Concert> getConcertsNeedingSync(){
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        return concertRepository.findKopisConcertsNeedingSync(threshold);
    }

    // 통계 메서드
    public List<Object[]> getConcertCountByGenre(){
        return concertRepository.countByGenre();
    }

    public List<Object[]> getConcertCountByArea(){
        return concertRepository.countByArea();
    }

    // 특별 조건 조회 메서드
    public List<Concert> getOngoingConcerts(){
        return concertRepository.findByStateOrderByStartDateAsc("공연중");
    }

    public List<Concert> getOpenRunConcerts(){
        return concertRepository.findByIsOpenRunTrueOrderByStartDateDesc();
    }

    public List<Concert> getChildPerformances(){
        return concertRepository.findByIsChildPerformanceTrueOrderByStartDateAsc();
    }

}
