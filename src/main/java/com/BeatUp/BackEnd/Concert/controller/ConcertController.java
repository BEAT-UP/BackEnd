package com.BeatUp.BackEnd.Concert.controller;


import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.service.ConcertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/concert")
public class ConcertController {

    @Autowired
    private ConcertService concertService;

    @GetMapping("/concerts")
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
}
