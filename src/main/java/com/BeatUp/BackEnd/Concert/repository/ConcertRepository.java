package com.BeatUp.BackEnd.Concert.repository;

import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.enums.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, UUID>, ConcertCustomRepository {

    // KOPIS 기반 조회
    Optional<Concert> findByKopisId(String kopisId);

    List<Concert> findByDataSource(DataSource dataSource);

    @Query("SELECT c FROM Concert c WHERE c.dataSource = 'KOPIS' AND" +
            "(c.lastSyncedAt IS NULL OR c.lastSyncedAt < :threshold)")
    List<Concert> findKopisConcertsNeedingSync(@Param("threshold") LocalDateTime threshold);

    // 이름 또는 장소로 검색
    @Query("SELECT c FROM Concert c WHERE " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.venue) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:date IS NULL OR (c.startDate >= :date AND (c.endDate IS NULL OR c.endDate >= :date))) " +
            "ORDER BY c.startDate ASC")
    List<Concert> findByQueryAndDate(
            @Param("query") String query,
            @Param("date") LocalDate date);

    // LocalDateTime 호환성
    default List<Concert> findByQueryAndDate(String query, LocalDateTime startOfDay, LocalDateTime endOfDay){
        LocalDate date = startOfDay != null ? startOfDay.toLocalDate()  : null;
        return findByQueryAndDate(query, date);
    }

    // 조건검색
    List<Concert> findByStateOrderByStartDateAsc(String state);

    List<Concert> findByIsOpenRunTrueOrderByStartDateDesc();

    List<Concert> findByIsChildPerformanceTrueOrderByStartDateAsc();

    // 통계 쿼리
    @Query("SELECT c.genre, COUNT(c) FROM Concert c WHERE c.genre IS NOT NULL GROUP BY c.genre ORDER BY COUNT(c) DESC")
    List<Object[]> countByGenre();

    @Query("SELECT c.area, COUNT(c) FROM Concert c WHERE c.area IS NOT NULL GROUP BY c.area ORDER BY COUNT(c) DESC")
    List<Object[]> countByArea();
}
