package com.BeatUp.BackEnd.RideRequest.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class RideRequestResponse {

    private UUID id;
    private UUID concertId;
    private String direction;
    private Double destLat;
    private Double destLng;
    private String status;
    private UUID matchGroupId;
    private LocalDateTime createdAt;

    // 기본 생성자
    public RideRequestResponse() {}

    // 생성자
    public RideRequestResponse(UUID id, UUID concertId, String direction,
                               Double destLat, Double destLng, String status,
                               UUID matchGroupId, LocalDateTime createdAt) {
        this.id = id;
        this.concertId = concertId;
        this.direction = direction;
        this.destLat = destLat;
        this.destLng = destLng;
        this.status = status;
        this.matchGroupId = matchGroupId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConcertId() { return concertId; }
    public void setConcertId(UUID concertId) { this.concertId = concertId; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }

    public Double getDestLng() { return destLng; }
    public void setDestLng(Double destLng) { this.destLng = destLng; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getMatchGroupId() { return matchGroupId; }
    public void setMatchGroupId(UUID matchGroupId) { this.matchGroupId = matchGroupId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
