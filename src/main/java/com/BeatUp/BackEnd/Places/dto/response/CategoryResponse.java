package com.BeatUp.BackEnd.Places.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String name; // 사용자 친화적 이름
    private String code; // 카카오 카테고리 코드
}
