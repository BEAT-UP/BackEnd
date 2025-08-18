package com.BeatUp.BackEnd.repository.Match;

import com.BeatUp.BackEnd.entity.Match.MatchGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchGroupRespository extends JpaRepository<MatchGroup, UUID> {
    // 기본 CRUD 메서드만 사용
}
