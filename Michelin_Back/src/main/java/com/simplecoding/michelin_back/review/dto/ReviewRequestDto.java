package com.simplecoding.michelin_back.review.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    private Long restaurantId;     // 어느 식당인지
    private Long memberId;         // 누가 썼는지
    private String content;        // 내용
    private Integer rating;        // 별점 (1~5)
    private Long parentReviewId;   // 답글일 경우 부모 ID (원글이면 null)
}