package com.BeatUp.BackEnd.RideRequest.repository;

import com.BeatUp.BackEnd.RideRequest.dto.RideMatchCondition;
import com.BeatUp.BackEnd.RideRequest.entity.RideRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RideRequestCustomRepository {

    Optional<RideRequest> findDuplicateRequest(UUID userId, UUID concertId, String direction);
    List<RideRequest> findMatchableRequests(RideMatchCondition rideMatchCondition);
}
