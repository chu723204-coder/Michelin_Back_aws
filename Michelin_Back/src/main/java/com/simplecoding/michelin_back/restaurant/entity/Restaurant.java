package com.simplecoding.michelin_back.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RESTAURANTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurants_seq")
    @SequenceGenerator(name = "restaurants_seq", sequenceName = "RESTAURANTS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "RESTAURANT_NAME", nullable = false)
    private String restaurantName;

    @Column(name = "CITY")
    private String city;

    @Column(name = "DISTRICT")
    private String district;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "GRADE", nullable = false)
    private String grade;

    @Column(name = "IS_GREEN_STAR", length = 1)
    private String isGreenStar = "N";

    @Column(name = "VIEW_COUNT")
    private Integer viewCount = 0;

    @Column(name = "LAT", nullable = false)
    private Double lat;

    @Column(name = "LNG", nullable = false)
    private Double lng;

    @Column(name = "KAKAO_PLACE_URL", length = 500)
    private String kakaoPlaceUrl;

    @Column(name = "KAKAO_PLACE_ID", length = 50)
    private String kakaoPlaceId;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "STATUS")
    private String status = "ACTIVE"; // "ACTIVE" 혹은 "DELETED"만 들어와야 함

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantImage> images = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    // ✅ 정보 수정 - kakaoPlaceId 추가
    public void update(String restaurantName, String city, String district,
                       String address, String phone, String grade,
                       String isGreenStar, Double lat, Double lng,
                       String kakaoPlaceUrl, String kakaoPlaceId, String category) {
        this.restaurantName = restaurantName;
        this.city = city;
        this.district = district;
        this.address = address;
        this.phone = phone;
        this.grade = grade;
        this.isGreenStar = isGreenStar;
        this.lat = lat;
        this.lng = lng;
        this.kakaoPlaceUrl = kakaoPlaceUrl;
        this.kakaoPlaceId = kakaoPlaceId;
        this.category = category;
    }
    // Restaurant.java 클래스 내부 아래쪽에 추가하세요
    public void softDelete() {
        this.status = "DELETED";
    }
}