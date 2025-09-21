package com.BeatUp.BackEnd.Match.taxi.service;


import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Match.entity.MatchGroup;
import com.BeatUp.BackEnd.Match.repository.MatchGroupRepository;
import com.BeatUp.BackEnd.Match.taxi.dto.response.TaxiServiceResponse;
import com.BeatUp.BackEnd.common.util.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
        // 거리 계산(Harvesine 공식)
        double distance = GeoUtils.calculateDistance(pickupLat, pickupLng, destLat, destLng); // km
        int estimatedTime = GeoUtils.calculateEstimateTime(distance);

        // 기본 요금 계산
        int baseFare = 3800; // 기본요금(2km까지)
        int distanceFare = 0;

        if(distance > 2.0){
            distanceFare = (int)Math.ceil((distance - 2.0) / 0.142) * 100; // 100원/142m
        }

        // 시간 요금 (정차 시간 추정)
        int timeFare = (int) Math.ceil(estimatedTime * 0.3) * 100; // 35초당 100원, 정차시간 30% 추정

        // 할증 계산
        double surcharge = 1.0;
        LocalTime now = LocalTime.now();
        if(now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(4, 0))){
            surcharge += 0.2; // 심야할증 20%
        }
        if(distance > 10.0){
            surcharge += 0.2; // 거리할증 20%
        }

        int totalFare = (int) Math.round((baseFare + distanceFare + timeFare) * surcharge);

        return new TaxiServiceResponse("카카오T", totalFare, estimatedTime);
    }

    private TaxiServiceResponse getUberPrice(double pickupLat, double pickupLng, double destLat, double destLng) {
        double distance = GeoUtils.calculateDistance(pickupLat, pickupLng, destLat, destLng);
        int estimatedTime = GeoUtils.calculateEstimateTime(distance);

        // UberX 기준
        int baseFare = 2500;
        int distanceFare = (int) Math.round(distance * 200); // 200원/km
        int timeFare = estimatedTime * 100;

        // 수요 할증 (시간대별, 지역별)
        double surgeMultiplier = calculateSurgeMultiplier(pickupLat, pickupLng);

        int totalFare = (int) Math.round((baseFare + distanceFare + timeFare) * surgeMultiplier);
        totalFare = Math.max(3500, totalFare); // 최소요금 3500원
        return new TaxiServiceResponse("우버", totalFare, estimatedTime);
    }

    private double calculateSurgeMultiplier(double lat, double lng){
        // 간단한 수요할증 계산
        LocalTime now  = LocalTime.now();
        double baseMultliplier = 1.0;

        // 출퇴근 시간 할증
        if((now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9,0))) ||
                (now.isAfter(LocalTime.of(18,0)) && now.isBefore(LocalTime.of(20, 0)))){
            baseMultliplier += 0.5;
        }

        // 주말 할증
        if(LocalDate.now().getDayOfWeek() == DayOfWeek.SATURDAY ||
            LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY){
            baseMultliplier += 0.3;
        }

        return Math.min(3.0, baseMultliplier); // 최대 3배
    }

    private TaxiServiceResponse getTadaPrice(double pickupLat, double pickupLng, double destLat, double destLng) {
        double distance = GeoUtils.calculateDistance(pickupLat, pickupLng, destLat, destLng);
        int estimatedTime = GeoUtils.calculateEstimateTime(distance);

        // 타다 일반 기준
        int baseFare = 4000; // 3km까지
        int distanceFare = 0;

        if (distance > 3.0) {
            distanceFare = (int) Math.round((distance - 3.0) * 200); // 200원/km
        }

        int timeFare = estimatedTime * 100; // 100원/분

        // 수요할증
        double surgeMultiplier = calculateTadaSurgeMultiplier(pickupLat, pickupLng);

        int totalFare = (int) Math.round((baseFare + distanceFare + timeFare) * surgeMultiplier);
        totalFare = Math.max(5000, totalFare); // 최소요금 5,000원

        return new TaxiServiceResponse("타다", totalFare, estimatedTime);
    }

    private double calculateTadaSurgeMultiplier(double lat, double lng) {
        // 타다 수요할증 계산
        LocalTime now = LocalTime.now();
        double baseMultiplier = 1.0;

        // 심야 할증
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
            baseMultiplier += 0.3;
        }

        // 출퇴근 시간 할증
        if ((now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 0))) ||
                (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 0)))) {
            baseMultiplier += 0.4;
        }

        return Math.min(2.5, baseMultiplier); // 최대 2.5배
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
