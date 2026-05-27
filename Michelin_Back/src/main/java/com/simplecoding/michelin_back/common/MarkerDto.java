package com.simplecoding.michelin_back.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkerDto {
    private Long id;           // 식당 고유 ID
    private String restaurantName;      // 식당 이름
    private Double lat;        // 위도
    private Double lng;        // 경도
    private String grade;      // 미쉐린 등급 (3스타, 빕 구르망 등)
    private String phone;
    private String address;
    // 마커 아이콘 색상을 결정할 필드 (프론트엔드 전달용)
    private String markerColor;
    private String category;
    private String imageUrl;
}
