package com.BeatUp.BackEnd.Concert.repository;

import com.BeatUp.BackEnd.Concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, UUID> {

    // 이름 또는 장소로 검색
    @Query("SELECT c FROM Concert c WHERE " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.venue) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:startOfDay IS NULL OR (c.startAt >= :startOfDay AND c.startAt <= :endOfDay)) " +
            "ORDER BY c.startAt ASC")
    List<Concert> findByQueryAndDate(
            @Param("query") String query,
            @Param("startOfDay")LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}
