package com.BeatUp.BackEnd.Places.Mapper;


import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    // 사용자 친화적 카테고리 -> 카카오 코드 매핑
    private static final Map<String, List<String>> CATEGORY_MAPPING = Map.of(
            "음식점", Arrays.asList("FD6"), // 일반 음식점
            "카페", Arrays.asList("CE7"), // 카페
            "패스트푸드", Arrays.asList("FD7"), // 패스트푸드
            "편의시설", Arrays.asList("CS2", "PK6"), // 편의점만
            "주차", Arrays.asList("PK6"), // 주차장만
            "오락거리", Arrays.asList("AT4", "CT1") // 관광명소 + 문화시설
    );

    // 카카오 코드 -> 사용자 친화적 이름 매핑
    private static final Map<String, String> CODE_TO_NAME = Map.of(
            "FD6", "음식점",
            "FD7", "패스트푸드",
            "CE7", "카페",
            "CS2", "편의점",
            "PK6", "주차장",
            "AT4", "관광명소",
            "CT1", "문화시설"
    );

    // 기본 카테고리
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList("FD6", "CE7", "CS2");

    /**
     * 사용자 카테코리 목록을 카카오 코드 목록으로 변환
     */
    public List<String> getKakaoCategoryCodes(List<String> userCategories){
        if(userCategories == null || userCategories.isEmpty()){
            return DEFAULT_CATEGORIES;
        }

        return userCategories.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(category -> !category.isEmpty())
                .flatMap(category -> getKakaoCategoryCodes(category).stream())
                .distinct()
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 단일 사용자 카테고리를 카카오 코드로 변환
     */
    public List<String> getKakaoCategoryCodes(String userCategory){
        if(userCategory == null || userCategory.isEmpty()){
            return DEFAULT_CATEGORIES;
        }

        return CATEGORY_MAPPING.getOrDefault(userCategory.trim(),
                Arrays.asList("FD6")); // 기본값: 음식점
    }

    /**
     * 카카오 코드를 사용자 친화적 이름으로 변환
     */
    public String getUserFriendlyName(String kakaoCode){
        return CODE_TO_NAME.getOrDefault(kakaoCode, "기타");
    }

    /**
     * 지원하는 모든 카테고리 목록 반환 (API 문서용)
     */
    public Set<String> getSupportedCategories(){
        return CATEGORY_MAPPING.keySet();
    }

    /**
     * 카테고리 유효성 검증
     */
    public boolean isValidCategory(String category){
        return category != null && CATEGORY_MAPPING.containsKey(category.trim());
    }
}
