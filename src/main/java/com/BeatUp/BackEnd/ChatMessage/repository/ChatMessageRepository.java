package com.BeatUp.BackEnd.ChatMessage.repository;

import com.BeatUp.BackEnd.ChatMessage.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    // 특정 방의 메시지 조회(선착순, 특정 시간 이후)
    List<ChatMessage> findByRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(UUID roomId, LocalDateTime createdAt);

    // 특정 방의 최근 메시지 조회(초기 로딩용, 최신 50개)
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(UUID roomId);
}
