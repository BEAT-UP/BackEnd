package com.BeatUp.BackEnd.Chat.ChatMessage.repository;

import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    // 페이징 조회
    List<ChatMessage> findByRoomId(UUID roomId, Pageable pageable);

    List<ChatMessage> findByRoomIdAndCreatedAtAfter(UUID roomId, LocalDateTime createdAt, Pageable pageable);
}
