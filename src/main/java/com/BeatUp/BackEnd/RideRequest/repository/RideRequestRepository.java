package com.BeatUp.BackEnd.RideRequest.repository;


import com.BeatUp.BackEnd.RideRequest.entity.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, UUID> {

    // 사용자의 특정 공연 + 방향 PENDING 요청 찾기(중복 방지용)
    @Query("SELECT r FROM RideRequest r WHERE r.userId = :userId " +
    "AND r.concertId = :concertId AND r.direction = :direction " +
    "AND r.status = 'PENDING'")
    Optional<RideRequest> findPendingByUserAndConcertAndDirection(
            @Param("userId") UUID userId,
            @Param("concertId") UUID concertId,
            @Param("direction") String direction
    );

    // 사용자의 모든 요청 조회(최신순)
    List<RideRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // PENDING 상태 요청들 조회 (매칭 워커용)
    List<RideRequest> findByStatusOrderByCreatedAt(String status);
}
