package com.BeatUp.BackEnd.Chat.ChatRoom.repository;

import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.common.repository.BaseRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends BaseRepository<ChatRoom> {
    // subjectId와 type으로 방 찾기(매칭 그룹 ID, 게시글 ID로 방 찾을 때 사용)
    Optional<ChatRoom> findBySubjectIdAndType(UUID subjectId, String type);

    @Override
    default String getEntityName(){
        return "ChatRoom";
    }
}
