package com.simplecoding.michelin_back.review.service;

import com.simplecoding.michelin_back.notification.sse.SseEmitters; // SSE 주입
import com.simplecoding.michelin_back.review.dto.ReviewRequestDto;
import com.simplecoding.michelin_back.review.entity.RestaurantReview;
import com.simplecoding.michelin_back.review.entity.ReviewReaction;
import com.simplecoding.michelin_back.review.repository.RestaurantReviewRepository;
import com.simplecoding.michelin_back.review.repository.ReviewReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final RestaurantReviewRepository reviewRepository;
    private final ReviewReactionRepository reactionRepository;
    private final SseEmitters sseEmitters; // 1. SSE 이미터 주입 추가

    /**
     * 1. 리뷰 및 답글 등록
     */
    @Transactional
    public Long createReview(ReviewRequestDto dto) {
        RestaurantReview.RestaurantReviewBuilder builder = RestaurantReview.builder()
                .restaurantId(dto.getRestaurantId())
                .memberId(dto.getMemberId())
                .content(dto.getContent())
                .rating(dto.getRating())
                .isDeleted("N"); // 기본값 명시

        if (dto.getParentReviewId() != null) {
            RestaurantReview parent = reviewRepository.findById(dto.getParentReviewId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 리뷰가 존재하지 않습니다."));
            builder.parent(parent);
        }

        Long savedId = reviewRepository.save(builder.build()).getReviewId();

        // [선택] 리뷰 등록 시에도 실시간 알림을 보내고 싶다면 추가
        // sseEmitters.broadcast("review_create", dto.getRestaurantId());

        return savedId;
    }

    /**
     * 2. 리뷰 소프트 삭제 (SSE 브로드캐스트 추가)
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        RestaurantReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));

        // DB 상태 변경
        review.softDelete();

        // 2. 실시간 알림 발송 (핵심!)
        // 현재 접속 중인 모든 유저에게 "이 리뷰 삭제됐으니 화면에서 지워!"라고 신호를 보냅니다.
        // 데이터로는 삭제된 리뷰의 ID나 해당 식당 ID를 보내면 리액트에서 처리하기 쉽습니다.
        sseEmitters.broadcast("review_update", "deletedId:" + reviewId);
    }

    /**
     * 3. 좋아요 / 싫어요 토글 로직
     */
    @Transactional
    public void toggleReaction(Long reviewId, Long memberId, String type) {
        RestaurantReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        Optional<ReviewReaction> existingReaction = reactionRepository.findByMemberIdAndReview(memberId, review);

        if (existingReaction.isPresent()) {
            ReviewReaction reaction = existingReaction.get();
            if (reaction.getReactionType().equals(type)) {
                reactionRepository.delete(reaction);
            } else {
                reaction.setReactionType(type);
            }
        } else {
            ReviewReaction newReaction = ReviewReaction.builder()
                    .review(review)
                    .memberId(memberId)
                    .reactionType(type)
                    .build();
            reactionRepository.save(newReaction);
        }

        // 3. 좋아요 변경 시에도 실시간으로 숫자를 갱신해주고 싶다면 브로드캐스트 호출
        sseEmitters.broadcast("review_update", "reactionChanged:" + reviewId);
    }

    /**
     * 4. 식당별 평균 별점 조회
     */
    public Double getAverageRating(Long restaurantId) {
        Double avg = reviewRepository.getAverageRating(restaurantId);
        return (avg != null) ? Math.round(avg * 10) / 10.0 : 0.0;
    }
}