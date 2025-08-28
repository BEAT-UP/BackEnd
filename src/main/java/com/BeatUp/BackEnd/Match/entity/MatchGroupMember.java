package com.BeatUp.BackEnd.Match.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "match_group_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_member_ride_request",
                columnNames = {"ride_request_id"}
        )
)
public class MatchGroupMember extends BaseEntity {

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
    protected MatchGroupMember(){}

    // 생성자
    public MatchGroupMember(UUID matchGroupId, UUID userId, UUID rideRequestId){
        this.matchGroupId = matchGroupId;
        this.userId = userId;
        this.rideRequestId = rideRequestId;
    }

    @Override
    public String toString(){
        return "MatchGroupMember{matchGroupId=" + matchGroupId + ", userId=" + userId + "}";
    }
}
