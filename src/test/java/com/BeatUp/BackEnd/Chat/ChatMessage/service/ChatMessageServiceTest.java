package com.BeatUp.BackEnd.Chat.ChatMessage.service;

import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.entity.ChatRoom;
import com.BeatUp.BackEnd.Chat.ChatRoom.repository.ChatRoomRepository;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import com.BeatUp.BackEnd.Match.taxi.service.TaxiComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private TaxiComparisonService taxiComparisonService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatRoom matchChatRoom;
    private ChatRoom normalChatRoom;

    @BeforeEach
    void setUp() {
        // 매칭 채팅방 설정
        UUID matchGroupId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        matchChatRoom = new ChatRoom("MATCH", matchGroupId, "동승 매칭 그룹", creatorId);

        // 일반 채팅방 설정
        UUID communityId = UUID.randomUUID();
        normalChatRoom = new ChatRoom("COMMUNITY", communityId, "커뮤니티 채팅방", creatorId);
    }

    @Test
    @DisplayName("택시 명령어 처리 - 매칭 채팅방에서 정상 동작")
    void handleSlashCommand_TaxiCommandInMatchRoom_ProcessesSuccessfully() {
        // Given
        UUID roomId = matchChatRoom.getId();
        UUID userId = UUID.randomUUID();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(matchChatRoom));
        
        List<TaxiServiceResponse> mockResponses = List.of(
                new TaxiServiceResponse("카카오T", 8000, 15),
                new TaxiServiceResponse("우버", 7500, 12)
        );
        when(taxiComparisonService.compareService(any(UUID.class))).thenReturn(mockResponses);
        when(taxiComparisonService.formatTaxiMessage(mockResponses)).thenReturn("택시 가격 비교 결과");

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/택시");

        // Then
        verify(chatRoomRepository).findById(roomId);
        verify(taxiComparisonService).compareService(matchChatRoom.getSubjectId());
        verify(taxiComparisonService).formatTaxiMessage(mockResponses);
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("택시 명령어 처리 - 일반 채팅방에서 차단")
    void handleSlashCommand_TaxiCommandInNormalRoom_BlocksCommand() {
        // Given
        UUID roomId = normalChatRoom.getId();
        UUID userId = UUID.randomUUID();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(normalChatRoom));

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/택시");

        // Then
        verify(chatRoomRepository).findById(roomId);
        verify(taxiComparisonService, never()).compareService(any(UUID.class));
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("택시 명령어 처리 - 채팅방 없음")
    void handleSlashCommand_ChatRoomNotFound_HandlesGracefully() {
        // Given
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/택시");

        // Then
        verify(chatRoomRepository).findById(roomId);
        verify(taxiComparisonService, never()).compareService(any(UUID.class));
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("택시 명령어 처리 - 서비스 에러")
    void handleSlashCommand_ServiceError_HandlesGracefully() {
        // Given
        UUID roomId = matchChatRoom.getId();
        UUID userId = UUID.randomUUID();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(matchChatRoom));
        when(taxiComparisonService.compareService(any(UUID.class)))
                .thenThrow(new RuntimeException("서비스 에러"));

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/택시");

        // Then
        verify(chatRoomRepository).findById(roomId);
        verify(taxiComparisonService).compareService(matchChatRoom.getSubjectId());
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("다양한 택시 명령어 인식 테스트")
    void handleSlashCommand_VariousTaxiCommands_AllRecognized() {
        // Given
        UUID roomId = matchChatRoom.getId();
        UUID userId = UUID.randomUUID();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(matchChatRoom));
        when(taxiComparisonService.compareService(any(UUID.class))).thenReturn(List.of());
        when(taxiComparisonService.formatTaxiMessage(any())).thenReturn("택시 가격 비교 결과");

        // When & Then
        chatMessageService.handleSlashCommand(userId, roomId, "/택시");
        chatMessageService.handleSlashCommand(userId, roomId, "/taxi");
        chatMessageService.handleSlashCommand(userId, roomId, "/가격");
        chatMessageService.handleSlashCommand(userId, roomId, "/요금");

        // 모든 명령어가 택시 서비스를 호출하는지 확인
        verify(taxiComparisonService, times(4)).compareService(any(UUID.class));
    }

    @Test
    @DisplayName("도움말 명령어 처리 테스트")
    void handleSlashCommand_HelpCommand_ProcessesSuccessfully() {
        // Given
        UUID roomId = matchChatRoom.getId();
        UUID userId = UUID.randomUUID();

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/도움말");

        // Then
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("알 수 없는 명령어 처리 테스트")
    void handleSlashCommand_UnknownCommand_HandlesGracefully() {
        // Given
        UUID roomId = matchChatRoom.getId();
        UUID userId = UUID.randomUUID();

        // When
        chatMessageService.handleSlashCommand(userId, roomId, "/알수없는명령어");

        // Then
        verify(chatMessageRepository, atLeastOnce()).save(any(ChatMessage.class));
    }
}
