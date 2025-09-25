package com.BeatUp.BackEnd.common.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * 모든 엔티티의 기본 클래스
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * id: UUID 자동 생성
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * createdAt: 생성 시간 자동 기록
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * updatedAt: 수정 시간 자동 기록
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // equals & hashCode(UUID 기반)
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }

}
