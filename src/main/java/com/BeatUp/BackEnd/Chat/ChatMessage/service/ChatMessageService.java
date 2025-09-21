package com.BeatUp.BackEnd.Chat.ChatMessage.service;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.request.ChatMessageRequest;
import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatMember;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatMemberRepsoitory;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatRoomRepository;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import com.BeatUp.BackEnd.Match.taxi.service.TaxiComparisonService;
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
    private TaxiComparisonService taxiComparisonService;

    @Autowired
    private UserProfileRepository userProfileRepository;


    // Websocket 메시지 전송 로직
    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, UUID senderId, ChatMessageRequest request){
        // 1. 방 존재 여부 확인
        chatRoomRepository.findById(roomId)
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


    public void handleSlashCommand(UUID userId, UUID roomId, String content){
        String command = content.toLowerCase().trim();

        switch (command){
            case "/택시":
            case "/taxi":
            case "/가격":
            case "/요금":
                handleTaxiCommand(roomId);
                break;
            case "/도움말":
            case "/help":
                handleHelpCommand(roomId);
                break;
            default:
                handleUnknownCommand(roomId, content);
                break;
        }
    }

    // 메시지 내역 조회 로직
    public List<ChatMessageResponse> getMessages(UUID roomId, UUID userId, LocalDateTime since){
        // 1. 방 존재 여부 확인
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        // 2. 방 맴버만 메시지 조회 가능(방 맴버인지)
        ChatMember membership = chatMemberRepsoitory.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 맴버가 아닙니다"));

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

    // 택시 명령어 처리
    private void handleTaxiCommand(UUID roomId){
        try{
            // 채팅방의 매칭 그룹 ID 조회
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

            if(!"MATCH".equals(chatRoom.getType())){
                sendSystemMessage(roomId, " 이 명령어는 매칭 채팅방에서만 사용할 수 있습니다.");
                return;
            }

            // 매칭 그룹 ID로 택시 서비스 비교
            UUID matchGroupId = chatRoom.getSubjectId();
            List<TaxiServiceResponse> taxiOptions = taxiComparisonService.compareService(matchGroupId);
            
            // 택시 가격 비교 메시지 포맷팅 및 전송
            String taxiMessage = taxiComparisonService.formatTaxiMessage(taxiOptions);
            sendSystemMessage(roomId, taxiMessage);
            
        }catch(Exception e){
            sendSystemMessage(roomId, " 택시 정보를 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            System.err.println("택시 명령어 처리 오류: " + e.getMessage());
        }
    }

    private void sendSystemMessage(UUID roomId, String content){
        ChatMessage systemMessage = new ChatMessage(roomId, content);
        chatMessageRepository.save(systemMessage);
    }

    private void handleHelpCommand(UUID roomId) {
        String helpMessage = """
             **사용 가능한 명령어**
             
             `/택시` - 택시 서비스 가격 비교
             `/taxi` - 택시 서비스 가격 비교 (영어)
             `/가격` - 택시 서비스 가격 비교
             `/요금` - 택시 서비스 가격 비교
             `/도움말` - 이 도움말 보기
             `/help` - 이 도움말 보기 (영어)
             
             명령어는 `/`로 시작해야 합니다.
             """;
        
        sendSystemMessage(roomId, helpMessage);
    }

    private void handleUnknownCommand(UUID roomId, String command) {
        String message = String.format(" 알 수 없는 명령어입니다: `%s`\n\n사용 가능한 명령어를 보려면 `/도움말`을 입력하세요.", command);
        sendSystemMessage(roomId, message);
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
