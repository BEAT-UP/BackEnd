package com.BeatUp.BackEnd.Match.taxi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaxiServiceResponse {
    private String serviceName; // "카카오T", "우버", "타다"
    private Integer estimatePrice; // 예상요금
    private Integer estimatedTime; // 예상 소요시간(분)
    private String deeplink; // 예약 딥링크
    private String weblink; // 웹 예약 링크
    private List<String> discounts; // 적용 가능한 할인

    // 기본 생성자
    public TaxiServiceResponse() {
    }

    // 모든 필드를 포함한 생성자
    public TaxiServiceResponse(String serviceName, Integer estimatePrice, Integer estimatedTime, 
                              String deeplink, String weblink, List<String> discounts) {
        this.serviceName = serviceName;
        this.estimatePrice = estimatePrice;
        this.estimatedTime = estimatedTime;
        this.deeplink = deeplink;
        this.weblink = weblink;
        this.discounts = discounts;
    }

    // 필수 필드만 포함한 생성자 (편의용)
    public TaxiServiceResponse(String serviceName, Integer estimatePrice, Integer estimatedTime) {
        this.serviceName = serviceName;
        this.estimatePrice = estimatePrice;
        this.estimatedTime = estimatedTime;
    }
}
