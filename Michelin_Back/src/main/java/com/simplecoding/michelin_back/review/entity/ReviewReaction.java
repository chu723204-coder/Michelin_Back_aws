package com.simplecoding.michelin_back.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW_REACTIONS",
        uniqueConstraints = {
                // 한 사용자가 한 리뷰에 대해 중복 반응 방지
                @UniqueConstraint(name = "UQ_MEMBER_REVIEW_REACTION", columnNames = {"MEMBER_ID", "REVIEW_ID"})
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REVIEW_REACTIONS")
    @SequenceGenerator(name = "SEQ_REVIEW_REACTIONS", sequenceName = "SEQ_REVIEW_REACTIONS", allocationSize = 1)
    @Column(name = "REACTION_ID")
    private Long reactionId;

    // 반응이 달린 리뷰 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private RestaurantReview review;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "REACTION_TYPE", nullable = false, length = 10)
    private String reactionType; // 'LIKE' 또는 'DISLIKE'

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
}