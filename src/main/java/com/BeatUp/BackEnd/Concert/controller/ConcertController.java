package com.BeatUp.BackEnd.Concert.controller;


import com.BeatUp.BackEnd.Concert.dto.ConcertSearchCondition;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.enums.DataSource;
import com.BeatUp.BackEnd.Concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/concert", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ConcertController {

    @Autowired
    private ConcertService concertService;

    @GetMapping(value = "/concerts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConcerts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date){

        List<Concert> concerts = concertService.getConcerts(query, date);

        return Map.of(
                "concerts", concerts,
                "total", concerts.size(),
                "query", query != null ? query: "",
                "date", date != null ? date.toString() : ""
        );
    }

    @GetMapping("/concerts/{id}")
    public ResponseEntity<Concert> getConcertById(@PathVariable UUID id){
        return concertService.getConcertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 고급 검색 API
    @GetMapping("/concerts/search")
    public Map<String, Object> searchConcerts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) DataSource dataSource,
            @RequestParam(required = false) Boolean isOpenRun,
            @RequestParam(required = false) Boolean isChildPerformance,
            @RequestParam(required = false) Boolean isDaehakro,
            @RequestParam(required = false) Boolean isFestival,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
            ){

        ConcertSearchCondition condition = ConcertSearchCondition.builder()
                .query(query)
                .date(date)
                .startDate(startDate)
                .endDate(endDate)
                .genre(genre)
                .state(state)
                .area(area)
                .dataSource(dataSource)
                .isOpenRun(isOpenRun)
                .isChildPerformance(isChildPerformance)
                .isDaehakro(isDaehakro)
                .isFestival(isFestival)
                .sortBy(sortBy)
                .build();

        List<Concert> concerts = concertService.getConcerts(condition);

        return Map.of(
                "concerts", concerts,
                "total", concerts.size(),
                "searchCondition", Map.of(
                        "query", query != null ? query : "",
                        "date", date != null ? date.toString(): "",
                        "genre", genre != null ? genre : "",
                        "state", state != null ? state : "",
                        "area", area != null ? area : ""
                )
        );
    }

    @GetMapping("/concerts/search/paging")
    public Page<Concert> searchConcertsWithPaging(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Boolean isOpenRun,
            Pageable pageable
    ){
        ConcertSearchCondition condition = ConcertSearchCondition.builder()
                .query(query)
                .date(date)
                .genre(genre)
                .state(state)
                .area(area)
                .isOpenRun(isOpenRun)
                .build();

        return concertService.searchConcertsWithPaging(condition, pageable);
    }

    // KOPIS ID 기반 조회
    @GetMapping("/concerts/kopis/{kopisId}")
    public ResponseEntity<Concert> getConcertByKopisId(@PathVariable String kopisId){
        return concertService.getConcertByKopisId(kopisId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 통계 API
    @GetMapping("/concerts/stats/genre")
    public Map<String, Object> getConcertStatsByGenre(){
        List<Object[]> stats = concertService.getConcertCountByGenre();
        return Map.of("genreStats", stats);
    }

    @GetMapping("/concerts/stats/area")
    public Map<String, Object> getConcertStatsByArea(){
        List<Object[]> stats = concertService.getConcertCountByArea();
        return Map.of("areaStats", stats);
    }

    // 특별 조건 조회 API

    @GetMapping("/concerts/ongoing")
    public Map<String, Object> getOngoingConcerts(){
        List<Concert> concerts = concertService.getOngoingConcerts();
        return Map.of(
                "concerts", concerts,
                "total", concerts.size(),
                "description", "현재 진행중인 공연 목록"
        );
    }

    @GetMapping("/concerts/openrun")
    public Map<String, Object> getOpenRunConcerts(){
        List<Concert> concerts = concertService.getOpenRunConcerts();
        return Map.of(
                "concerts", concerts,
                "total", concerts.size(),
                "description", "오픈런 공연 목록(최신순)"
        );
    }

    @GetMapping("/concets/children")
    public Map<String, Object> getChildPerformances(){
        List<Concert> concerts = concertService.getChildPerformances();
        return Map.of(
                "concerts", concerts,
                "total", concerts.size(),
                "description", "아동 공연 목록"
        );
    }


}
