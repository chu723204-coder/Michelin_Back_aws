package com.simplecoding.michelin_back.review.repository;

import com.simplecoding.michelin_back.review.entity.ReviewReaction;
import com.simplecoding.michelin_back.review.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {

    // 1. 특정 사용자가 특정 리뷰에 남긴 반응 찾기 (좋아요 취소나 변경 시 사용)
    Optional<ReviewReaction> findByMemberIdAndReview(Long memberId, RestaurantReview review);

    // 2. 특정 리뷰의 좋아요(LIKE) 개수 카운트
    long countByReviewAndReactionType(RestaurantReview review, String reactionType);

    // 3. 특정 사용자가 반응을 이미 남겼는지 여부 확인
    boolean existsByMemberIdAndReview(Long memberId, RestaurantReview review);
}