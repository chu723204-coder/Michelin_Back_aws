package com.simplecoding.michelin_back.restaurant.repository;

import com.simplecoding.michelin_back.restaurant.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {

    // 음식점 ID로 전체 이미지 조회
    List<RestaurantImage> findByRestaurantId(Long restaurantId);

    // 음식점 ID로 대표 이미지 조회
    Optional<RestaurantImage> findByRestaurantIdAndIsMain(Long restaurantId, String isMain);

    // 음식점 ID로 이미지 전체 삭제
    void deleteByRestaurantId(Long restaurantId);
}