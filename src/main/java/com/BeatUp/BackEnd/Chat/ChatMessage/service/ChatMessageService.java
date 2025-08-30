package com.BeatUp.BackEnd.Chat.ChatMessage.service;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.request.ChatMessageRequest;
import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatMember;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatMemberRepsoitory;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatRoomRepository;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepsoitory chatMemberRepsoitory;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Websocket 메시지 전송 로직
    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, UUID senderId, ChatMessageRequest request){
        // 1. 방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        // 2. 맴버십 확인(현재 참여 중인 맴버만)
        ChatMember membership = chatMemberRepsoitory.findByRoomIdAndUserId(roomId, senderId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 맴버가 아닙니다"));

        if(membership.getLeftAt() != null){
            throw new IllegalArgumentException("이미 나간 채팅방입니다");
        }

        // 3. 메시지 저장
        ChatMessage message = new ChatMessage(roomId, senderId, request.getContent());
        ChatMessage savedMessage = chatMessageRepository.save(message);

        System.out.println("메시지 저장 완료 - ID: " + savedMessage.getId());

        // 4. DTO로 변환하여 반환
        return mapToChatMessageResponse(savedMessage);
    }

    // 메시지 내역 조회 로직
    public List<ChatMessageResponse> getMessages(UUID roomId, UUID userId, LocalDateTime since){
        // 1. 방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        // 2. 방 맴버만 메시지 조회 가능(방 맴버인지)
        ChatMember membership = chatMemberRepsoitory.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 맴버강 아닙니다"));

        if(membership.getLeftAt() != null){
            throw new IllegalArgumentException("이미 나간 채팅방입니다");
        }

        // 3. 메시지 조회(since 파라미터 활용)
        List<ChatMessage> messages;
        if(since != null){
            // since 이후 메시지 페이징 조회 (오래된 순 정렬)
            Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").ascending());
            messages = chatMessageRepository.findByRoomIdAndCreatedAtAfter(roomId, since, pageable);
        }else{
            // 최근 50개 (createdAt 내림차순으로 가져와서 -> 오래된 순으로 정렬)
            Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
            messages = chatMessageRepository.findByRoomId(roomId, pageable);
            Collections.reverse(messages); // 오래된 것부터 표시
        }

        // 4. DTO로 변환
        return messages.stream()
                .map(this::mapToChatMessageResponse)
                .collect(Collectors.toList());
    }

    // 딥링크 생성 로직
    public String generateDeeplink(UUID roomId, UUID userId, String kind){

        // 1. 맴버십 확인
        ChatMember membership = chatMemberRepsoitory.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 맴버가 아닙니다"));

        if(membership.getLeftAt() != null){
            throw new IllegalArgumentException("이미 나간 채팅방입니다");
        }

        // 2. 딥링크 생성 로직
        if(!"payment".equals(kind)){
            throw new IllegalArgumentException("지원하지 않는 딥링크 종류입니다.");
        }

        String url = "beatup://pay?roomId=" + roomId + "&perUser=12000&currency=KRW";

        // 3. 시스템 메시지로 링크 안내
        ChatMessage linkMessage = new ChatMessage(roomId,
                "결제 링크가 생성되었습니다: " + url);
        chatMessageRepository.save(linkMessage);

        return url;
    }

    // ChatMessage -> DTO 변환
    private ChatMessageResponse mapToChatMessageResponse(ChatMessage message){
        String senderName = "시스템";

        if(message.getSenderId() != null){
            senderName = userProfileRepository.findByUserId(message.getSenderId())
                    .map(profile -> profile.getNickname() != null ? profile.getNickname(): "익명")
                    .orElse("익명");
        }

        return new ChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                senderName,
                message.getType(),
                message.getCreatedAt(),
                message.getContent()
        );
    }
}
