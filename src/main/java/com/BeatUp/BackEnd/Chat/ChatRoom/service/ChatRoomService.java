package com.BeatUp.BackEnd.Chat.ChatRoom.service;


import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.request.CreateRoomRequest;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.response.RoomResponse;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatMember;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatMemberRepsoitory;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatRoomRepository;
import com.BeatUp.BackEnd.Community.Post.repository.PostRepository;
import com.BeatUp.BackEnd.Match.repository.MatchGroupRepository;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepsoitory chatMemberRepsoitory;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // subjectId 검증용(필요한 Repository들)
    @Autowired
    private MatchGroupRepository matchGroupRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public RoomResponse createRoom(UUID createdBy, CreateRoomRequest request){
        // 1. 프로필 완성 여부 확인
        UserProfile profile = userProfileRepository.findByUserId(createdBy)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        if(!profile.isProfileCompleted()){
            throw new IllegalArgumentException("프로필을 먼저 완성해주세요");
        }

        // 2. subjectId 존재 여부 검증
        validateSubjectId(request.getType(), request.getSubjectId());

        // 3. 중복 방 생성 방지(선택적)
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findBySubjectIdAndType(request.getSubjectId(), request.getType());
        if(existingRoom.isPresent()){
            throw new IllegalArgumentException("이미 해당 주체의 채팅방이 존재합니다");
        }

        // 4. 채팅방 생성
        ChatRoom chatRoom = new ChatRoom(request.getType(), request.getSubjectId(), request.getTitle(), createdBy);
        chatRoom.setMaxMembers(request.getMaxMembers());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 5. 생성자 자동 참여
        ChatMember creatorMember = new ChatMember(savedRoom.getId(), createdBy);
        chatMemberRepsoitory.save(creatorMember);

        // 6. 시스템 메시지 추가
        ChatMessage welcomeMessage =new ChatMessage(savedRoom.getId(), "채팅방이 생성되었습니다. 참여자들과 대화를 시작해보세요!");
        chatMessageRepository.save(welcomeMessage);

        return mapToRoomResponse(savedRoom);
    }

    @Transactional
    public void joinRoom(UUID userId, UUID roomId){
        // 1. 프로필 완성 여부 확인
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        if(!profile.isProfileCompleted()){
            throw new IllegalArgumentException("프로필을 먼저 완성해주세요");
        }

        // 2. 방 존재 및 상태 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if(!"OPEN".equals(chatRoom.getStatus())){
            throw new IllegalArgumentException("입장할 수 없는 채팅방입니다");
        }

        // 3. 중복 참여 확인(현재 참여 중인 맴버)
        Optional<ChatMember> existingMember = chatMemberRepsoitory
                .findByRoomIdAndUserId(roomId, userId);

        if(existingMember.isPresent() && existingMember.get().getLeftAt() == null){
            return; // 이미 참여 중이면 무시(멱등성)
        }

        // 4. 정원 초과 확인
        long currentMembers = chatMemberRepsoitory.countByRoomIdAndLeftAtIsNull(roomId);
        if(currentMembers >= chatRoom.getMaxMembers()){
            throw new IllegalArgumentException("채팅방 정원이 초과되었습니다.");
        }

        // 5. 맴버 추가
        ChatMember newMember = new ChatMember(roomId, userId);
        chatMemberRepsoitory.save(newMember);

        // 6. 참여 시스템 메시지
        ChatMessage joinMessage = new ChatMessage(roomId, "새로운 맴버가 채팅방에 참여했습니다.");
        chatMessageRepository.save(joinMessage);
    }

    // subjectId 유효성 검증
    private void validateSubjectId(String type, UUID subjectId){
        boolean exists = switch (type){
            case "MATCH" -> matchGroupRepository.existsById(subjectId);
            case "COMMUNITY" -> postRepository.existsById(subjectId);
            default -> throw new IllegalArgumentException("알 수 없는 채팅방 타입입니다");
        };

        if(!exists){
            throw new IllegalArgumentException("유효하지 않은 주제 ID입니다.");
        }
    }

    // 엔티티 -> DTO 변환
    private RoomResponse mapToRoomResponse(ChatRoom chatRoom){
        Long currentMembersCount =
                chatMemberRepsoitory.countByRoomIdAndLeftAtIsNull(chatRoom.getId());
        return new RoomResponse(
                chatRoom.getId(),
                chatRoom.getType(),
                chatRoom.getSubjectId(),
                chatRoom.getTitle(),
                chatRoom.getMaxMembers(),
                chatRoom.getStatus(),
                currentMembersCount,
                chatRoom.getCreatedAt()
        );
    }
}
