package com.BeatUp.BackEnd.Match.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "match_group_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_member_ride_request",
                columnNames = {"ride_request_id"}
        )
)
public class MatchGroupMemeber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name="match_group_id")
    private UUID matchGroupId;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, name = "ride_request_id")
    private UUID rideRequestId;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // 기본 생성자
    protected MatchGroupMemeber(){}

    // 생성자
    public MatchGroupMemeber(UUID matchGroupId, UUID userId, UUID rideRequestId){
        this.matchGroupId = matchGroupId;
        this.userId = userId;
        this.rideRequestId = rideRequestId;
    }

    // Getters
    public UUID getId(){return id;}
    public UUID getMatchGroupId(){return matchGroupId;}
    public UUID getUserId(){return userId;}
    public UUID getRideRequestId(){return rideRequestId;}
    public LocalDateTime getJoinedAt(){return joinedAt;}

    @Override
    public String toString(){
        return "MatchGroupMember{matchGroupId=" + matchGroupId + ", userId=" + userId + "}";
    }
}
