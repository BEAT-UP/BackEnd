package com.BeatUp.BackEnd.repository.Match;

import com.BeatUp.BackEnd.entity.Match.MatchGroupMemeber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchGroupMemberRepository extends JpaRepository<MatchGroupMemeber, UUID> {

    // 특정 매칭 그룹의 맴버 조회(나중에 Chat 연동 시 사용)
    List<MatchGroupMemeber> findByMatchGroupId(UUID matchGroupId);
}
