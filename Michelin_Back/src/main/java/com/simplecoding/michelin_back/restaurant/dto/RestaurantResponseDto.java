package com.simplecoding.michelin_back.restaurant.dto;

import com.simplecoding.michelin_back.restaurant.entity.Restaurant;
import com.simplecoding.michelin_back.restaurant.entity.RestaurantImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RestaurantResponseDto {

    private Long id;
    private String restaurantName;
    private String grade;
    private String city;
    private String district;
    private Double lat;
    private Double lng;
    private String isGreenStar;
    private String address;
    private String kakaoPlaceUrl;
    private String kakaoPlaceId;
    private String phone;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String mainImageUrl;
    private List<String> imageUrls;
    private Double distance;
    private String category; // ✅ 추가

    public RestaurantResponseDto(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.restaurantName = restaurant.getRestaurantName();
        this.grade = restaurant.getGrade();
        this.city = restaurant.getCity();
        this.district = restaurant.getDistrict();
        this.lat = restaurant.getLat();
        this.lng = restaurant.getLng();
        this.isGreenStar = restaurant.getIsGreenStar();
        this.address = restaurant.getAddress();
        this.kakaoPlaceUrl = restaurant.getKakaoPlaceUrl();
        this.kakaoPlaceId = restaurant.getKakaoPlaceId();
        this.phone = restaurant.getPhone();
        this.viewCount = restaurant.getViewCount();
        this.createdAt = restaurant.getCreatedAt();
        this.updatedAt = restaurant.getUpdatedAt();
        this.distance = null;
        this.category = restaurant.getCategory(); // ✅ 추가

        List<RestaurantImage> images = restaurant.getImages() != null
                ? restaurant.getImages()
                : List.of();

        this.mainImageUrl = images.stream()
                .filter(img -> "Y".equals(img.getIsMain()))
                .map(img -> img.getImageUrl())
                .findFirst()
                .orElse(null);

        this.imageUrls = images.stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}