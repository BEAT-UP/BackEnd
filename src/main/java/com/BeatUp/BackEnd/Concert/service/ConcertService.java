package com.BeatUp.BackEnd.Concert.service;


import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;

    // 공연 목록 조회(검색 + 날짜 필터)
    public List<Concert> getConcerts(String query, LocalDate date){
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        // LocalDate를 LocalDateTime 범위로 변환
        if(date != null)
        {
            startOfDay = date.atStartOfDay(); // 해당 날짜의 00:00:00
            endOfDay = date.atTime(LocalTime.MAX); // 해당 날짜의 23:59:59.9999
        }

        return concertRepository.findByQueryAndDate(query, startOfDay, endOfDay);
    }

    // 공연 상세 조회
    public Optional<Concert> getConcertById(UUID id){
        return concertRepository.findById(id);
    }
}
