package com.BeatUp.BackEnd.Match.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "match_group")
public class MatchGroup extends BaseEntity {

    @Column(nullable = false, name="concert_id")
    private UUID concertId;

    @Column(nullable = false)
    private String direction; // "TO_VENUE" | "FROM_VENUE"

    @Column(nullable = false, name = "dest_bucket")
    private String destBucket; // "37.50.127.02" 형태의 좌표 버킷

    private Integer capacity = 4;

    @Column(nullable = false)
    private String status = "OPEN";

    // 기본 생성자
    protected MatchGroup(){}

    // 생성자
    public MatchGroup(UUID concertId, String direction, String destBucket){
        this.concertId = concertId;
        this.direction = direction;
        this.destBucket = destBucket;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return "MatchGroupId=" + getId() + ", concertId=" + concertId +
                ", direction= " + direction + ",bucket=" + destBucket + "}";
    }
}
