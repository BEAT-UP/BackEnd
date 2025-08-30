package com.BeatUp.BackEnd.RideRequest.repository;

import com.BeatUp.BackEnd.RideRequest.dto.RideMatchCondition;
import com.BeatUp.BackEnd.RideRequest.entity.RideRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.BeatUp.BackEnd.RideRequest.entity.QRideRequest.rideRequest;
import static com.BeatUp.BackEnd.common.util.QueryDslUtils.eqlfNotNull;
import static com.BeatUp.BackEnd.common.util.QueryDslUtils.hasText;

@Repository
@RequiredArgsConstructor
public class RideRequestCustomRepositoryImpl implements RideRequestCustomRepository{


    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RideRequest> findDuplicateRequest(UUID userId, UUID concertId, String direction){
        RideRequest result = queryFactory
                .selectFrom(rideRequest)
                .where(
                        rideRequest.userId.eq(userId),
                        rideRequest.concertId.eq(concertId),
                        rideRequest.direction.eq(direction),
                        rideRequest.status.eq("PENDING")
                )
                .fetchFirst();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RideRequest> findMatchableRequests(RideMatchCondition condition){
        return queryFactory
                .selectFrom(rideRequest)
                .where(
                        concertIdEq(condition.getConcertId()),
                        directionEq(condition.getDirection()),
                        statusEq(condition.getStatus()),
                        requestedAfter(condition.getRequestedAfter())
                )
                .orderBy(rideRequest.createdAt.asc()) // 선착순
                .fetch();
    }

    // 동적 조건 메서드들
    private BooleanExpression concertIdEq(UUID concertId){
        return eqlfNotNull(rideRequest.concertId, concertId);
    }

    private BooleanExpression directionEq(String direction){
        return eqlfNotNull(rideRequest.direction, direction);
    }

    private BooleanExpression statusEq(String status){
        return hasText(status) ? rideRequest.status.eq(status) : rideRequest.status.ne("CANCELLED");
    }

    private BooleanExpression requestedAfter(LocalDateTime dateTime){
        return dateTime != null ? rideRequest.createdAt.after(dateTime) : null;
    }
}
