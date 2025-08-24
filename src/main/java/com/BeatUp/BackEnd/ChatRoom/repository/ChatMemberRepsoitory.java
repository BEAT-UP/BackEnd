package com.BeatUp.BackEnd.ChatRoom.repository;


import com.BeatUp.BackEnd.ChatRoom.entity.ChatMember;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMemberRepsoitory {

    // 특정 방의 모든 맴버 조회
    List<ChatMember> findByRoomId(UUID roomId);

    // 특정 방에 특정 유저가 있는지 확인
    Optional<ChatMember> findByRoomAndUserId(UUID roomId, UUID userId);

    // 특정 방의 현재 맴버 수 조회(left_at이 null인 맴버만)
    long countByRoomIdAndLeftAtIsNull(UUID record);
}
