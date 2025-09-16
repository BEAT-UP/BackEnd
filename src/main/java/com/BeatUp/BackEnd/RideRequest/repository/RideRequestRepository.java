package com.BeatUp.BackEnd.RideRequest.repository;


import com.BeatUp.BackEnd.RideRequest.entity.RideRequest;
import com.BeatUp.BackEnd.common.repository.StatusRepository;
import com.BeatUp.BackEnd.common.repository.UserOwnedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRequestRepository extends StatusRepository<RideRequest>, UserOwnedRepository<RideRequest> {

    // 사용자의 특정 공연 + 방향 PENDING 요청 찾기(중복 방지용)
    @Query("SELECT r FROM RideRequest r WHERE r.userId = :userId " +
    "AND r.concertId = :concertId AND r.direction = :direction " +
    "AND r.status = 'PENDING'")
    Optional<RideRequest> findPendingByUserAndConcertAndDirection(
            @Param("userId") UUID userId,
            @Param("concertId") UUID concertId,
            @Param("direction") String direction
    );

    // StatusRepository 활용 - PENDING 상태 조회
    default List<RideRequest> findPendingOrderByCreatedAt() {
        return findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Override
    default String getEntityName() {
        return "RideRequest";
    }
}
