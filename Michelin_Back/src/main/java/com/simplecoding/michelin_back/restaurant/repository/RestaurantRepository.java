package com.simplecoding.michelin_back.restaurant.repository;

import com.simplecoding.michelin_back.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>,
        JpaSpecificationExecutor<Restaurant> {

    // ── P2 - 검색 자동완성 (이름 LIKE + 조회수 높은 순) ─────────────────
    @Query("SELECT r FROM Restaurant r " +
            "WHERE r.restaurantName LIKE %:keyword% " +
            "ORDER BY r.viewCount DESC")
    List<Restaurant> findAutocomplete(@Param("keyword") String keyword, Pageable pageable);

    // ── P2 - 등급별 조회 ─────────────────────────────────────────────────
    Page<Restaurant> findByGrade(String grade, Pageable pageable);

    // ── P2 - 도시별 조회 ─────────────────────────────────────────────────
    Page<Restaurant> findByCity(String city, Pageable pageable);

    // ── P2/P4 - 반경 내 음식점 조회 (위도/경도 기반) ────────────────────
    @Query(value = "SELECT * FROM (" +
            "  SELECT r.*, " +
            "  (6371 * acos(cos(?1 * 3.1415926535 / 180) * cos(r.lat * 3.1415926535 / 180) * " +
            "  cos((r.lng * 3.1415926535 / 180) - (?2 * 3.1415926535 / 180)) + " +
            "  sin(?1 * 3.1415926535 / 180) * sin(r.lat * 3.1415926535 / 180))) AS distance " +
            "  FROM RESTAURANTS r" +
            ") WHERE distance <= ?3 ORDER BY distance", nativeQuery = true)
    List<Restaurant> findRestaurantsWithinRadius(Double lat, Double lng, Double radius);

    // ── P4 - 이름 키워드 검색 (대소문자 무시) ───────────────────────────
    List<Restaurant> findByRestaurantNameContainingIgnoreCase(String name);
}