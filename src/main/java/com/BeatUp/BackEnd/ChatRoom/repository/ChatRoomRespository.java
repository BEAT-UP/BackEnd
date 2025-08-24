package com.BeatUp.BackEnd.ChatRoom.repository;

import com.BeatUp.BackEnd.ChatRoom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRespository extends JpaRepository<ChatRoom, UUID> {
    // subjectId와 type으로 방 찾기(매칭 그룹 ID, 게시글 ID로 방 찾을 때 사용)
    Optional<ChatRoom> findBySubjectIdAndType(UUID subjectId, String type);
}
