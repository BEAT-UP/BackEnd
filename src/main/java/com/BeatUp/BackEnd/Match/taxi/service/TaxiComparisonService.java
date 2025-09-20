package com.BeatUp.BackEnd.Match.taxi.service;


import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Match.entity.MatchGroup;
import com.BeatUp.BackEnd.Match.repository.MatchGroupRepository;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TaxiComparisonService {

    @Autowired
    private MatchGroupRepository matchGroupRepository;

    @Autowired
    private ConcertRepository concertRepository;

    public List<TaxiServiceResponse> compareService(UUID matchGroupId){
        // 1. 매칭 그룹 정보 조회
        MatchGroup matchGroup = matchGroupRepository.findById(matchGroupId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 그룹을 찾을 수 없습니다."));

        // 2. 공연장 정보 조회
        Concert concert = concertRepository.findById(matchGroup.getConcertId())
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));

        // 3. 목적지 좌표 파싱 (destBucket에서)
        String[] coords = matchGroup.getDestBucket().split(",");
        double destLat = Double.parseDouble(coords[0]);
        double destLng = Double.parseDouble(coords[1]);

        // 4. 출발지 좌표 설정 (공연장 좌표 사용)
        double pickupLat, pickupLng;
        
        if (concert.getVenueLat() != null && concert.getVenueLng() != null) {
            // DB에 저장된 공연장 좌표 사용
            pickupLat = concert.getVenueLat();
            pickupLng = concert.getVenueLng();
        } else {
            // 좌표가 없는 경우 기본값 사용 (서울 중심가)
            pickupLat = 37.5665;
            pickupLng = 126.9780;
            System.out.println("공연장 좌표가 없어 기본값 사용 - venue: " + concert.getVenue());
        }

        // 5. 각 서비스별 요금 조회
        List<TaxiServiceResponse> responses = new ArrayList<>();

        // 카카오T 요금 조회
        try{
            TaxiServiceResponse kakaoT = getKakaoTPrice(pickupLat, pickupLng, destLat, destLng);
            responses.add(kakaoT);
        } catch (Exception e) {
            System.err.println("카카오T 요금 조회 실패: " + e.getMessage());
        }

        // 우버 요금 조회
        try{
            TaxiServiceResponse uber = getUberPrice(pickupLat, pickupLng, destLat, destLng);
            responses.add(uber);
        } catch (Exception e) {
            System.err.println("우버 요금 조회 실패: " + e.getMessage());
        }

        // 타다 요금 조회
        try{
            TaxiServiceResponse tada = getTadaPrice(pickupLat, pickupLng, destLat, destLng);
            responses.add(tada);
        } catch (Exception e) {
            System.err.println("타다 요금 조회 실패: " + e.getMessage());
        }

        return responses;
    }

    private TaxiServiceResponse getKakaoTPrice(double pickupLat, double pickupLng, double destLat, double destLng){
        // 카카오 API 호출 로직
        // 실제 구현시 카카오 모빌리티 API 사용
        return new TaxiServiceResponse("카카오T", 8000, 15);
    }

    private TaxiServiceResponse getUberPrice(double pickupLat, double pickupLng, double destLat, double destLng) {
        // 우버 API 호출 로직
        return new TaxiServiceResponse("우버", 7500, 12);
    }

    private TaxiServiceResponse getTadaPrice(double pickupLat, double pickupLng, double destLat, double destLng) {
        // 타다 API 호출 로직
        return new TaxiServiceResponse("타다", 9000, 18);
    }

    public String formatTaxiMessage(List<TaxiServiceResponse> options){
        if(options.isEmpty()){
            return "택시 서비스 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요.";
        }

        StringBuilder message = new StringBuilder("택시 서비스 가격 비교\n");

        // 거리 순으로 정렬
        options.sort(Comparator.comparing(TaxiServiceResponse::getEstimatePrice));

        for(int i = 0; i < options.size(); i++){
            TaxiServiceResponse option = options.get(i);

            message.append(String.format("%s **%s**; %,d원 (%d분)\n",
                    option.getServiceName(),
                    option.getEstimatePrice(),
                    option.getEstimatedTime()));
        }

        message.append("\n 원하는 서비스를 선택하시면 예약 페이지로 이동합니다.");
        message.append("\n🔄 다시 조회하려면 `/택시` 명령어를 사용하세요.");

        return message.toString();
    }
}
