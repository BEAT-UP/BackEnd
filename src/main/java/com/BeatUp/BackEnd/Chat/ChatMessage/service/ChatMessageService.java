package com.BeatUp.BackEnd.Chat.ChatMessage.service;


import com.BeatUp.BackEnd.Chat.ChatMessage.dto.request.ChatMessageRequest;
import com.BeatUp.BackEnd.Chat.ChatMessage.dto.response.ChatMessageResponse;
import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatMember;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatMemberRepsoitory;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatRoomRepository;
import com.BeatUp.BackEnd.FCM.dto.FcmNotificationMessage;
import com.BeatUp.BackEnd.FCM.service.producer.FcmNotificationProducer;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import com.BeatUp.BackEnd.Match.taxi.service.TaxiComparisonService;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.common.util.MonitoringUtil;
import com.BeatUp.BackEnd.common.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepsoitory chatMemberRepsoitory;
    private final ChatMessageRepository chatMessageRepository;
    private final TaxiComparisonService taxiComparisonService;
    private final UserProfileRepository userProfileRepository;
    private final MonitoringUtil monitoringUtil;
    
    @Autowired(required = false)
    private FcmNotificationProducer fcmNotificationProducer;

    /**
     * ChatMessage에서 허용할 정렬 필드
     */
    private static final Set<String> ALLOWED_CHAT_MESSAGE_SORT_FIELDS = Set.of("createdAt");


    // Websocket 메시지 전송 로직
    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, UUID senderId, ChatMessageRequest request){
        var sample = monitoringUtil.startApiCallTimer("chat.message.save");
        
        try {
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

            log.debug("메시지 저장 완료 - ID: {}", savedMessage.getId());

            // 메트릭 기록
            monitoringUtil.recordApiCall(sample, "chat.message.save", "success");
            monitoringUtil.recordApiCall("chat.message.save", "success");

            // 4. FCM 알림 전송(발신자 제외)
            sendChatNotification(roomId, senderId, savedMessage);

            // 5. DTO로 변환하여 반환
            return mapToChatMessageResponse(savedMessage);
        } catch (IllegalArgumentException e) {
            monitoringUtil.recordApiCall(sample, "chat.message.save", "validation_error");
            monitoringUtil.recordApiCall("chat.message.save", "validation_error");
            throw e;
        } catch (Exception e) {
            monitoringUtil.recordApiCall(sample, "chat.message.save", "error");
            monitoringUtil.recordApiCall("chat.message.save", "error");
            log.error("메시지 저장 실패 - roomId: {}, senderId: {}", roomId, senderId, e);
            throw e;
        }
    }

    /**
     * 채팅방 맴버들에게 FCM 알림 전송(발신자 제외)
     */
    private void sendChatNotification(UUID roomId, UUID senderId, ChatMessage message){
        try{
            // 채팅방 맴버 조회(나간 맴버 조회)
            List<ChatMember> members = chatMemberRepsoitory.findByRoomId(roomId)
                    .stream()
                    .filter(m -> m.getLeftAt() != null && !m.getUserId().equals(senderId))
                    .collect(Collectors.toList());

            if(members.isEmpty()){
                return;
            }

            // 발신자 이름 조회
            String senderName = userProfileRepository.findByUserId(senderId)
                    .map(profile -> profile.getNickname() != null ? profile.getNickname() : "익명")
                    .orElse("익명");

            // 각 맴버에게 알림 전송
            if (fcmNotificationProducer != null) {
                for(ChatMember member: members){
                    FcmNotificationMessage notification = FcmNotificationMessage.builder()
                            .userId(member.getUserId())
                            .title("새 메시지")
                            .body(senderName + ": " + message.getContent())
                            .type("CHAT")
                            .data(Map.of(
                                    "roomId", roomId.toString(),
                                    "messageId", message.getId().toString(),
                                    "senderId", senderId.toString()
                            ))
                            .build();

                    fcmNotificationProducer.sendChatNotification(notification);
                }
            }

            log.debug("채팅 FCM 알림 전송 완료 - roomId: {}, 맴버 수: {}", roomId, members.size());
        } catch (Exception e) {
            log.error("채팅 FCM 알림 전송 실패 - roomId: {}", roomId, e);
        }
    }


    public void handleSlashCommand(UUID userId, UUID roomId, String content){
        var sample = monitoringUtil.startApiCallTimer("chat.command");
        String command = content.toLowerCase().trim();
        String commandType = "unknown";

        try {
            switch (command){
                case "/택시":
                case "/taxi":
                case "/가격":
                case "/요금":
                    commandType = "taxi";
                    handleTaxiCommand(roomId);
                    break;
                case "/도움말":
                case "/help":
                    commandType = "help";
                    handleHelpCommand(roomId);
                    break;
                default:
                    commandType = "unknown";
                    handleUnknownCommand(roomId, content);
                    break;
            }

            monitoringUtil.recordApiCall(sample, "chat.command", "success");
            monitoringUtil.recordApiCall("chat.command", "success");
            monitoringUtil.recordApiCall("chat.command.count", commandType);
        } catch (Exception e) {
            monitoringUtil.recordApiCall(sample, "chat.command", "error");
            monitoringUtil.recordApiCall("chat.command", "error");
            log.error("슬래시 명령어 처리 실패 - command: {}, roomId: {}", command, roomId, e);
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
            // PageableUtil 사용: 크기 제한 및 검증
            Pageable pageable = PageableUtil.createPageable(
                    0,
                    50, // 최대 50개(PageableUtil이 maxSize로 제한)
                    "createdAt",
                    "ASC",
                    ALLOWED_CHAT_MESSAGE_SORT_FIELDS
            );
            messages = chatMessageRepository.findByRoomIdAndCreatedAtAfter(roomId, since, pageable);
        }else{
            // 최근 50개 (createdAt 내림차순으로 가져와서 -> 오래된 순으로 정렬)
            Pageable pageable = PageableUtil.createPageable(
                    0,
                    50,
                    "createdAt",
                    "DESC",
                    ALLOWED_CHAT_MESSAGE_SORT_FIELDS
            );
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
