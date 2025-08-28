package com.BeatUp.BackEnd.Concert.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "concert")
public class Concert extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String venue;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    private String genre; // "K-POP", "뮤지컬", "콘서트" 등

    private String price; // "77000원" 형태

    // 생성자
    protected Concert(){}

    public Concert(String name, String venue, LocalDateTime startAt, String genre, String price){
        this.name = name;
        this.venue = venue;
        this.startAt = startAt;
        this.genre = genre;
        this.price = price;
    }

}
