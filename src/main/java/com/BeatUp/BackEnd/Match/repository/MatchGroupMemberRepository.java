package com.BeatUp.BackEnd.Match.repository;

import com.BeatUp.BackEnd.Match.entity.MatchGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchGroupMemberRepository extends JpaRepository<MatchGroupMember, UUID> {

    // 특정 매칭 그룹의 맴버 조회(나중에 Chat 연동 시 사용)
    List<MatchGroupMember> findByMatchGroupId(UUID matchGroupId);
}
