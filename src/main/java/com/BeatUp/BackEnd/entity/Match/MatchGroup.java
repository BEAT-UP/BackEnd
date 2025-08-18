package com.BeatUp.BackEnd.entity.Match;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_group")
public class MatchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name="concert_id")
    private UUID concertId;

    @Column(nullable = false)
    private String direction; // "TO_VENUE" | "FROM_VENUE"

    @Column(nullable = false, name = "dest_bucket")
    private String destBucket; // "37.50.127.02" 형태의 좌표 버킷

    private Integer capacity = 4;

    @Column(nullable = false)
    private String status = "OPEN";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    protected MatchGroup(){}

    // 생성자
    public MatchGroup(UUID concertId, String direction, String destBucket){
        this.concertId = concertId;
        this.direction = direction;
        this.destBucket = destBucket;
    }

    // Getters
    public UUID getId(){return id;}
    public UUID getConcertId(){return concertId;}
    public String getDirection(){return direction;}
    public String getDestBucket(){return destBucket;}
    public Integer getCapacity(){return capacity;}
    public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}

    // Setters
    public void setStatus(String status){this.status = status;}

    @Override
    public String toString(){
        return "MatchGroupId=" + id + ", concertId=" + concertId +
                ", direction= " + direction + ",bucket=" + destBucket + "}";
    }
}
