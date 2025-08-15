package com.BeatUp.BackEnd.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "concert")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String venue;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    private String genre; // "K-POP", "뮤지컬", "콘서트" 등

    private String price; // "77000원" 형태

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자
    protected Concert(){}

    public Concert(String name, String venue, LocalDateTime startAt, String genre, String price){
        this.name = name;
        this.venue = venue;
        this.startAt = startAt;
        this.genre = genre;
        this.price = price;
    }

    // Getters
    public UUID getId(){return id;}
    public String getName(){return name;}
    public String getVenue(){return venue;}
    public LocalDateTime getStartAt(){return startAt;}
    public String getGenre(){return genre;}
    public String getPrice(){return price;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
