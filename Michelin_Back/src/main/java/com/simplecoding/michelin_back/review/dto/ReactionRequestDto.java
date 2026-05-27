package com.simplecoding.michelin_back.review.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionRequestDto {
    private Long reviewId;
    private Long memberId;
    private String reactionType; // "LIKE" 또는 "DISLIKE"
}