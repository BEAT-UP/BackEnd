package com.BeatUp.BackEnd.Match.scheduler;

import com.BeatUp.BackEnd.Chat.ChatMessage.entity.ChatMessage;
import com.BeatUp.BackEnd.Chat.ChatMessage.repository.ChatMessageRepository;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.request.CreateRoomRequest;
import com.BeatUp.BackEnd.Chat.ChatRoom.dto.response.RoomResponse;
import com.BeatUp.BackEnd.Chat.ChatRoom.service.ChatRoomService;
import com.BeatUp.BackEnd.Match.entity.MatchGroup;
import com.BeatUp.BackEnd.Match.entity.MatchGroupMember;
import com.BeatUp.BackEnd.RideRequest.entity.RideRequest;
import com.BeatUp.BackEnd.Match.repository.MatchGroupMemberRepository;
import com.BeatUp.BackEnd.Match.repository.MatchGroupRepository;
import com.BeatUp.BackEnd.RideRequest.repository.RideRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MatchingScheduler {

    @Autowired
    private RideRequestRepository rideRequestRepository;

    @Autowired
    private MatchGroupRepository matchGroupRepository;

    @Autowired
    private MatchGroupMemberRepository matchGroupMemberRepository;

    @Autowired
    private ChatRoomService chatRoomService;


    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final int MIN_GROUP_SIZE = 3; // 최소 3명
    private static final int MAX_GROUP_SIZE = 4; // 최대 4명

    // 30초마다 매칭 워커 실행
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void processMatching(){
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println(" [" + timestamp + "] 매칭 워커 실행 중,,,");

        try{
            // 1. PENDING 상태 요청들 조회 (오래된 순서, 성능을 위해 최대 200건)
            List<RideRequest> pendingRequests = rideRequestRepository
                    .findPendingOrderByCreatedAt()
                    .stream()
                    .limit(200)
                    .collect(Collectors.toList());

            if(pendingRequests.isEmpty()){
                System.out.println("매칭 대기 중인 요청이 없습니다.");
                return;
            }

            System.out.println("매칭 대기 요청" + pendingRequests.size() + "개 발견");

            // 2. concertId + direction + destBucket으로 그룹핑
            Map<String, List<RideRequest>> buckets = pendingRequests.stream()
                    .collect(Collectors.groupingBy(this::createMatchingKey));

            int totalMatched = 0;
            int groupsCreated = 0;

            // 3. 각 버킷에서 매칭 시도
            for(Map.Entry<String, List<RideRequest>> entry: buckets.entrySet()){
                String key = entry.getKey();
                List<RideRequest> requests = entry.getValue();

                if(requests.size() >= MIN_GROUP_SIZE){
                    // 오래된 순으로 정렬 보장
                    requests.sort(Comparator.comparing(RideRequest::getCreatedAt));

                    // 최대 4명가지만 매칭
                    List<RideRequest> toMatch = requests.subList(0, Math.min(MAX_GROUP_SIZE, requests.size()));

                    UUID matchGroupId = createMatchGroup(toMatch);
                    totalMatched += toMatch.size();
                    groupsCreated++;

                    System.out.println("매칭 성공! 그룹 ID:" + matchGroupId + ", 키: " + key + ", 인원: " + toMatch.size() + "명");
                }else {
                    System.out.println("버킷[" + key + "]:" + requests.size() + "명 대기 중(최소 3명 필요)");
                }
            }

            System.out.println("매칭 완료:" + groupsCreated + "개 그룹, 총" + totalMatched + "명 매칭됨");
        }catch (Exception e){
            System.out.println("매칭 워커 에러: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 매칭 키 생성(concertId:direction:destBucket)
    private String createMatchingKey(RideRequest request){
        return request.getConcertId() + "." + request.getDirection() + "." + getDestBucket(request);
    }

    // 좌표를 버킷으로 변환
    private String getDestBucket(RideRequest request){
        double lat = roundToTwoDecimalPlaces(request.getDestLat());
        double lng = roundToTwoDecimalPlaces(request.getDestLng());
        return lat + "," + lng;
    }

    // 정확한 소수점 2자리 반올림
    private double roundToTwoDecimalPlaces(Double value){
        return new BigDecimal(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // 매칭 그룹 생성 및 맴버 등록
    private UUID createMatchGroup(List<RideRequest> requests){
        if(requests.isEmpty()){
            throw new IllegalArgumentException("매칭할 요청이 없습니다");
        }

        RideRequest firstRequest = requests.get(0);
        String destBucket = getDestBucket(firstRequest);

        // 1. MatchGroup 생성
        MatchGroup matchGroup = new MatchGroup(
                firstRequest.getConcertId(),
                firstRequest.getDirection(),
                destBucket
        );

        // 그룹 상태 설정
        if(requests.size() >= MAX_GROUP_SIZE){
            matchGroup.setStatus("FULL");
        }else {
            matchGroup.setStatus("OPEN");
        }

        MatchGroup savedGroup = matchGroupRepository.save(matchGroup);

        // 2. 각 요청 처리
        for(RideRequest request: requests){
            // RideRequest 상태 업데이트
            request.setStatus("MATCHED");
            request.setMatchGroupId(savedGroup.getId());
            rideRequestRepository.save(request);

            // MatchGroupMember 생성
            MatchGroupMember member = new MatchGroupMember(
                    savedGroup.getId(),
                    request.getUserId(),
                    request.getId()
            );
            matchGroupMemberRepository.save(member);

            System.out.println("- 요청 매칭됨:" + request.getId() + "(유저: " + request.getUserId() + ")");
        }

        createMatchChatRoom(savedGroup, requests);

        return savedGroup.getId();
    }

    // 매칭 그룹 생성 후 채팅방 생성
    private void createMatchChatRoom(MatchGroup matchGroup, List<RideRequest> matchedRequests){
        try{
            // 1. 채팅방 생성
            CreateRoomRequest chatRoomRequest = new CreateRoomRequest();
            chatRoomRequest.setType("MATCH");
            chatRoomRequest.setSubjectId(matchGroup.getId());
            chatRoomRequest.setTitle("동승 매칭 그룹");
            chatRoomRequest.setMaxMembers(matchedRequests.size());

            // 첫 번째 사용자를 생성자로 설정
            UUID creatorId = matchedRequests.get(0).getUserId();
            RoomResponse roomResponse = chatRoomService.createRoom(creatorId, chatRoomRequest);

            // 2. 매칭된 모든 사용자를 채팅방에 자동 초대
            for(RideRequest request: matchedRequests){
                if(!request.getUserId().equals(creatorId)){  // 생성자는 이미 자동 참여됨
                    chatRoomService.joinRoom(request.getUserId(), roomResponse.getId());
                }
            }

            // 3. 매칭 완료 시스템 메시지 추가
            ChatMessage matchCompleteMessage = new ChatMessage(
                    roomResponse.getId(),
                    "동승 매칭이 완료되었습니다!"
            );
            chatMessageRepository.save(matchCompleteMessage);

            System.out.println("채팅방 자동 생성 완료: " + roomResponse.getId());

        } catch (Exception e) {
            System.out.println("채팅방 생성 실패: " + e.getMessage());
        }
    }
}
