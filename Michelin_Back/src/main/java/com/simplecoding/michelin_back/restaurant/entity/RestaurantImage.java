package com.simplecoding.michelin_back.restaurant.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RESTAURANT_IMAGES")
@Getter
@NoArgsConstructor
public class RestaurantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurant_images_seq")
    @SequenceGenerator(name = "restaurant_images_seq", sequenceName = "RESTAURANT_IMAGES_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "IS_MAIN", length = 1)
    private String isMain = "N";

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Builder
    public RestaurantImage(Restaurant restaurant, String imageUrl,
                           String isMain, Integer sortOrder) {
        this.restaurant = restaurant;
        this.imageUrl = imageUrl;
        this.isMain = isMain != null ? isMain : "N";
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdAt = LocalDateTime.now();
    }

    // 대표 이미지 여부 변경
    public void updateIsMain(String isMain) {
        this.isMain = isMain;
    }
}