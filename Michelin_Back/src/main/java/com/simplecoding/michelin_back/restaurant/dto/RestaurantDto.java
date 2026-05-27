package com.simplecoding.michelin_back.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantDto {
    private Long id;
    private String restaurantName;
    private String grade;
    private String address;
    private String district;
    private Double lat;
    private Double lng;
    private Double distance;   // 내 위치로부터의 거리 (KM 단위)
    private String phone;
    private String imageUrl;
}
