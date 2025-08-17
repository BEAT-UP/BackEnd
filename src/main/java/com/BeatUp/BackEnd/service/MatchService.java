package com.BeatUp.BackEnd.service;


import com.BeatUp.BackEnd.dto.request.CreateRideRequest;
import com.BeatUp.BackEnd.dto.response.RideRequestResponse;
import com.BeatUp.BackEnd.entity.RideRequest;
import com.BeatUp.BackEnd.entity.User.UserProfile;
import com.BeatUp.BackEnd.repository.ConcertRepository;
import com.BeatUp.BackEnd.repository.RideRequestRepository;
import com.BeatUp.BackEnd.repository.User.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class MatchService {

    @Autowired
    private RideRequestRepository rideRequestRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public RideRequestResponse createRideRequest(UUID userId, CreateRideRequest request){
        // 1. 프로필 완성 여부 확인
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        if(!profile.isProfileCompleted()){
            throw new IllegalArgumentException("프로필을 먼저 완성해주세요");
        }

        // 2. 비즈니스 검증
        validateRideRequest(request);

        // 3. Concert 존재 여부 확인
        if(!concertRepository.existsById(request.getConcertId())){
            throw new IllegalArgumentException("존재하지 않는 공연장입니다.");
        }

        // 4. 중복 요청 확인
        Optional<RideRequest> existing = rideRequestRepository.findPendingByUserAndConcertAndDirection(
                userId, request.getConcertId(), request.getDirection()
        );

        if(existing.isPresent()){
            throw new IllegalArgumentException(
                    "이미 해당 공연에 대한 동승 요청이 있습니다. 기존 요청을 취소한 후 다시 시도하세요."
            );
        }

        // 5. 새 요청 생성
        RideRequest rideRequest = new RideRequest(
                userId,
                request.getConcertId(),
                request.getDirection(),
                request.getDestLat(),
                request.getDestLng(),
                request.getWindowStart(),
                request.getWindowEnd()
        );

        // 선택적 필드 생성
        if(request.getGenderPref() != null){
            rideRequest.setGenderPref(request.getGenderPref());
        }
        if(request.getAgeMin() != null){
            rideRequest.setAgeMin(request.getAgeMin());
        }
        if(request.getAgeMax() != null){
            rideRequest.setAgeMax(request.getAgeMax());
        }

        // 6. 저장
        RideRequest saved = rideRequestRepository.save(rideRequest);

        // 7. 응답 생성
        return mapToResponse(saved);
    }

    public Optional<RideRequestResponse> getRideRequest(UUID id){
        return rideRequestRepository.findById(id)
                .map(this::mapToResponse);
    }

    // 비즈니스 검증 로직
    public void validateRideRequest(CreateRideRequest request){
        // 시간 범위 검증
        if(request.getWindowStart().isAfter(request.getWindowEnd())){
            throw new IllegalArgumentException("탑승 시작 시간은 종료 시간보다 이전이어야 합니다");
        }

        // 나이 범위 검증
        if(request.getAgeMin() != null && request.getAgeMax() != null && request.getAgeMin() > request.getAgeMax()){
            throw new IllegalArgumentException("최소 나이는 최대 나이보다 작거나 같아야 합니다");
        }
    }

    // 엔티티 -> DTO 변환
    private RideRequestResponse mapToResponse(RideRequest rideRequest){
        return new RideRequestResponse(
                rideRequest.getId(),
                rideRequest.getConcertId(),
                rideRequest.getDirection(),
                rideRequest.getDestLat(),
                rideRequest.getDestLng(),
                rideRequest.getStatus(),
                rideRequest.getMatchGroupId(),
                rideRequest.getCreatedAt()
        );
    }
}
