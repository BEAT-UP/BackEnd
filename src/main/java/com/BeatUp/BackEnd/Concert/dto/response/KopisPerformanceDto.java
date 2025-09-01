package com.BeatUp.BackEnd.Concert.dto.response;


import com.BeatUp.BackEnd.Concert.enums.KopisGenre;
import com.BeatUp.BackEnd.Concert.enums.KopisPerformanceState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KopisPerformanceDto {

    // 공연 목록/상세 공통 필드
    @JsonProperty("mt20id")
    @NotBlank
    private String mt20id;          // 공연ID

    @JsonProperty("prfnm")
    private String prfnm;           // 공연명

    @JsonProperty("prfpdfrom")
    private String prfpdfrom;       // 공연시작일 (yyyy.MM.dd)

    @JsonProperty("prfpdto")
    private String prfpdto;         // 공연종료일 (yyyy.MM.dd)

    @JsonProperty("fcltynm")
    private String fcltynm;         // 공연시설명(공연장명)

    @JsonProperty("poster")
    private String poster;          // 공연포스터경로

    @JsonProperty("genrenm")
    private String genrenm;         // 공연장르명

    @JsonProperty("prfstate")
    private String prfstate;        // 공연상태

    // 목록 조회 추가 필드
    @JsonProperty("area")
    private String area;            // 공연지역

    @JsonProperty("openrun")
    private String openrun;         // 오픈런 (Y/N)

    // 상세 조회 전용 필드
    @JsonProperty("mt10id")
    private String mt10id;          // 공연시설ID

    @JsonProperty("prfcast")
    private String prfcast;         // 공연출연진

    @JsonProperty("prfcrew")
    private String prfcrew;         // 공연제작진

    @JsonProperty("prfruntime")
    private String prfruntime;      // 공연런타임

    @JsonProperty("prfage")
    private String prfage;          // 공연관람연령

    @JsonProperty("entrpsnmP")
    private String entrpsnmP;       // 제작사

    @JsonProperty("entrpsnmA")
    private String entrpsnmA;       // 기획사

    @JsonProperty("entrpsnmH")
    private String entrpsnmH;       // 주최

    @JsonProperty("entrpsnmS")
    private String entrpsnmS;       // 주관

    @JsonProperty("pcseguidance")
    private String pcseguidance;    // 티켓가격

    @JsonProperty("sty")
    private String sty;             // 줄거리

    @JsonProperty("dtguidance")
    private String dtguidance;      // 공연시간안내

    @JsonProperty("visit")
    private String visit;           // 내한 (Y/N)

    @JsonProperty("child")
    private String child;           // 아동 (Y/N)

    @JsonProperty("daehakro")
    private String daehakro;        // 대학로 (Y/N)

    @JsonProperty("festival")
    private String festival;        // 축제 (Y/N)

    @JsonProperty("musicallicense")
    private String musicallicense;  // 뮤지컬라이센스 (Y/N)

    @JsonProperty("musicalcreate")
    private String musicalcreate;   // 뮤지컬창작 (Y/N)

    @JsonProperty("updatedate")
    private String updatedate;      // 최종수정일

    // 유틸리티 메서드
    public LocalDate getParsedStartDate() {
        return parseKopisDate(this.prfpdfrom);
    }

    public LocalDate getParsedEndDate() {
        return parseKopisDate(this.prfpdto);
    }

    private LocalDate parseKopisDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return LocalDate.parse(dateString.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public boolean isValidPerformance() {
        return mt20id != null && !mt20id.trim().isEmpty() &&
                prfnm != null && !prfnm.trim().isEmpty();
    }

    public KopisPerformanceState getPerformanceState() {
        return KopisPerformanceState.fromCode(prfstate);
    }

    public KopisGenre getGenre() {
        return KopisGenre.fromCode(getGenreCodeFromName());
    }

    private String getGenreCodeFromName() {
        if (genrenm == null) return null;

        return switch (genrenm) {
            case "연극" -> "AAAA";
            case "뮤지컬" -> "GGGA";
            case "서양음악(클래식)" -> "CCCA";
            case "한국음악(국악)" -> "CCCC";
            case "대중음악" -> "CCCD";
            case "무용(서양/한국무용)" -> "BBBC";
            case "대중무용" -> "BBBE";
            case "복합" -> "EEEA";
            case "서커스/마술" -> "EEEB";
            default -> null;
        };
    }

    public boolean isOpenRun() {
        return "Y".equals(openrun);
    }

    public boolean isChildPerformance() {
        return "Y".equals(child);
    }
}
