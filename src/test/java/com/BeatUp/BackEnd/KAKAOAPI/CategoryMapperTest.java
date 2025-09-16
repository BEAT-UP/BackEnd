package com.BeatUp.BackEnd.KAKAOAPI;

import com.BeatUp.BackEnd.Places.Mapper.CategoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @Test
    @DisplayName("사용자 카테고리를 카카오 코드로 정확히 매핑")
    void getKakaoCategoryCodes_SingleCategory_Success(){
        // Given & When
        List<String> result = categoryMapper.getKakaoCategoryCodes("음식점");

        // Then
        assertThat(result).containsExactly("FD6");
    }

    @Test
    @DisplayName("편의시설은 편의점과 주차장 모두 매핑")
    void getKakaoCategoryCodes_ConvenienceCategory_MultipleCode(){
        // Given & When
        List<String> result = categoryMapper.getKakaoCategoryCodes("편의시설");

        // Then
        assertThat(result).containsExactlyInAnyOrder("CS2", "PK6");
    }

    @Test
    @DisplayName("여러 카테고리 입력 시 중복 제거하여 반환")
    void getKakaoCategoryCodes_MultipleCategories_Deduplicated(){
        // Given
        List<String> userCategories = Arrays.asList("음식점", "카페", "편의시설");

        // When
        List<String> result = categoryMapper.getKakaoCategoryCodes(userCategories);

        // Then
        assertThat(result).containsExactlyInAnyOrder("FD6", "CE7", "CS2", "PK6");
        assertThat(result).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("null 또는 빈 카테고리 반환 시 기본 값 반환")
    void getKakaoCategoryCodes_NullOrEmpty_DefaultCategories(){
        // When
        List<String> nullResult = categoryMapper.getKakaoCategoryCodes((List<String>) null);
        List<String> emptyResult = categoryMapper.getKakaoCategoryCodes(Collections.emptyList());

        // Then
        assertThat(nullResult).containsExactlyInAnyOrder("FD6", "CE7", "CS2");
        assertThat(emptyResult).containsExactlyInAnyOrder("FD6", "CE7", "CS2");
    }

    @Test
    @DisplayName("카카오 코드를 사용자 친화적 이름으로 반환")
    void getUserFriendlyName_ValidCode_CorrectName(){
        // When & Then
        assertThat(categoryMapper.getUserFriendlyName("FD6")).isEqualTo("음식점");
        assertThat(categoryMapper.getUserFriendlyName("CE7")).isEqualTo("카페");
        assertThat(categoryMapper.getUserFriendlyName("CS2")).isEqualTo("편의점");
        assertThat(categoryMapper.getUserFriendlyName("INVALID")).isEqualTo("기타");
    }

    @Test
    @DisplayName("카테고리 유효성 검증")
    void isValidCategory_ValidAndInvalid_CorrectValidation(){
        // When & Then
        assertThat(categoryMapper.isValidCategory("음식점")).isTrue();
        assertThat(categoryMapper.isValidCategory("카페")).isTrue();
        assertThat(categoryMapper.isValidCategory("존재하지 않는 카테고리")).isFalse();
        assertThat(categoryMapper.isValidCategory(null)).isFalse();
        assertThat(categoryMapper.isValidCategory("")).isFalse();
    }
}
