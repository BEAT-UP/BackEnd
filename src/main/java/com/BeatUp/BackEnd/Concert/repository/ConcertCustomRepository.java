package com.BeatUp.BackEnd.Concert.repository;

import com.BeatUp.BackEnd.Concert.dto.ConcertSearchCondition;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConcertCustomRepository {
    List<Concert> searchConcerts(ConcertSearchCondition condition);
    Page<Concert> searchConcerts(ConcertSearchCondition condition, Pageable pageable);
}
