package com.BeatUp.BackEnd.Concert.mapper;

import com.BeatUp.BackEnd.Concert.dto.response.KopisPerformanceDto;
import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.enums.DataSource;
import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ConcertKopisMapper {

    public static Concert fromKopisData(KopisPerformanceDto dto){
        Concert concert = new Concert();
        updateFromKopisData(concert, dto);
        concert.setDataSource(DataSource.KOPIS);
        return concert;
    }

    public static void updateFromKopisData(Concert concert, KopisPerformanceDto dto){
            concert.setKopisId(dto.getMt20id());
            concert.setFacilityId(dto.getMt10id());
            concert.setName(dto.getPrfnm());
            concert.setVenue(dto.getFcltynm());
            concert.setGenre(dto.getGenrenm());
            concert.setState(dto.getPrfstate());
            concert.setArea(dto.getArea());
            concert.setPosterUrl(dto.getPoster());
            concert.setCast(dto.getPrfcast());
            concert.setCrewInfo(dto.getPrfcrew());
            concert.setRuntime(dto.getPrfruntime());
            concert.setAgeLimit(dto.getPrfage());
            concert.setPrice(dto.getPcseguidance());
            concert.setSynopsis(dto.getSty());
            concert.setPerformanceTime(dto.getDtguidance());

            // 제작진 정보
            concert.setProducer(dto.getEntrpsnmP());
            concert.setPlanner(dto.getEntrpsnmA());
            concert.setHost(dto.getEntrpsnmH());
            concert.setOrganizer(dto.getEntrpsnmS());

            // 날짜 파싱
            concert.setStartDate(dto.getParsedStartDate());
            concert.setEndDate(dto.getParsedEndDate());

            // 플래그 정보
            concert.setIsOpenRun("Y".equals(dto.getOpenrun()));
            concert.setIsChildPerformance("Y".equals(dto.getChild()));
            concert.setIsDaehakro("Y".equals(dto.getDaehakro()));
            concert.setIsFestival("Y".equals(dto.getFestival()));
            concert.setIsMusicalLicense("Y".equals(dto.getMusicallicense()));
            concert.setIsMusicalCreate("Y".equals(dto.getMusicalcreate()));
            concert.setIsVisit("Y".equals(dto.getVisit()));

            // 동기화 시간 업데이트
            concert.setKopisUpdatedAt(parseKopisUpdateDate(dto.getUpdatedate()));
            concert.setLastSyncedAt(LocalDateTime.now());
    }

    private static LocalDateTime parseKopisUpdateDate(String updateDateString){
        if(updateDateString == null || updateDateString.trim().isEmpty()){
            return null;
        }
        
        String trimmed = updateDateString.trim();
        
        // 여러 패턴을 시도 (마이크로초 자릿수가 가변적이므로)
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss.SSSSSS",  // 6자리 마이크로초
            "yyyy-MM-dd HH:mm:ss.SSSSS",   // 5자리 마이크로초
            "yyyy-MM-dd HH:mm:ss.SSSS",    // 4자리 마이크로초
            "yyyy-MM-dd HH:mm:ss.SSS",     // 3자리 밀리초
            "yyyy-MM-dd HH:mm:ss"          // 마이크로초 없음
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeException e) {
                // 다음 패턴 시도
            }
        }
        
        log.warn("KOPIS updatedate 파싱 실패: '{}' - 모든 패턴 시도 실패", updateDateString);
        return null;
    }
}
