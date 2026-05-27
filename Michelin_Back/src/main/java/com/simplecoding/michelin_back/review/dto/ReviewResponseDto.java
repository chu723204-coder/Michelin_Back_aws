package com.simplecoding.michelin_back.review.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long memberId;
    private String content;
    private Integer rating;
    private String isDeleted;
    private LocalDateTime createdAt;

    // 좋아요 / 싫어요 개수 (화면에 바로 표시하기 위함)
    private long likeCount;
    private long dislikeCount;

    // 답글 목록 (대댓글 구조를 위해 자기 자신을 리스트로 가짐)
    private List<ReviewResponseDto> children;
}