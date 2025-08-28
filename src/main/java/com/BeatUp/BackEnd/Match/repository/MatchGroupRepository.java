package com.BeatUp.BackEnd.Match.repository;

import com.BeatUp.BackEnd.Match.entity.MatchGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchGroupRepository extends JpaRepository<MatchGroup, UUID> {
    // 기본 CRUD 메서드만 사용
}
