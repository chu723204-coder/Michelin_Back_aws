package com.simplecoding.michelin_back.social.repository;

import com.simplecoding.michelin_back.social.entity.RestaurantBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestaurantBookmarkRepository extends JpaRepository<RestaurantBookmark, Long> {
    // [수정] UserId -> MemberId
    Optional<RestaurantBookmark> findByMemberIdAndRestaurantId(Long memberId, Long restaurantId);
    boolean existsByMemberIdAndRestaurantId(Long memberId, Long restaurantId);
}