package com.BeatUp.BackEnd.RideRequest.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RideMatchCondition {
    private UUID userId;
    private UUID concertId;
    private String direction; // TO_VENUE, FROM_VENUE
    private String status; // PENDING, MATCHED
    private LocalDateTime requestedAfter;
}
