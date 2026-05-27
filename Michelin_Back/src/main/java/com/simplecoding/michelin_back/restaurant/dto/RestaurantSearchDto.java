package com.simplecoding.michelin_back.restaurant.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter  // ✅ 쿼리 파라미터 바인딩을 위해 추가
@NoArgsConstructor
public class RestaurantSearchDto {

    private String keyword;     // 검색어
    private String grade;       // 등급 필터 (1스타 / 빕 구르망 / 선정 레스토랑)
    private String city;        // 시/도 필터
    private String district;    // 구/동 필터
    private String isGreenStar; // 그린스타 필터
    private int page = 0;       // 페이지 번호 (0부터 시작)
    private int size = 12;      // 페이지 당 개수
}