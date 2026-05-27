package com.simplecoding.michelin_back.social.repository;

import com.simplecoding.michelin_back.social.entity.RestaurantLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantLikeRepository extends JpaRepository<RestaurantLike, Long> {
    // [수정] findByUserId -> findByMemberId
    Optional<RestaurantLike> findByMemberIdAndRestaurantId(Long memberId, Long restaurantId);
}
