package com.simplecoding.michelin_back.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RESTAURANT_REVIEWS")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANT_REVIEWS")
    @SequenceGenerator(name = "SEQ_RESTAURANT_REVIEWS", sequenceName = "SEQ_RESTAURANT_REVIEWS", allocationSize = 1)
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @Column(name = "RESTAURANT_ID", nullable = false)
    private Long restaurantId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "CONTENT", nullable = false, length = 4000)
    private String content;

    @Builder.Default
    @Column(name = "IS_DELETED", nullable = false, length = 10)
    private String isDeleted = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "RATING", nullable = false)
    private Integer rating = 5;

    // --- 답글(Self 참조) 구조 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_REVIEW_ID")
    private RestaurantReview parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RestaurantReview> children = new ArrayList<>();

    // --- 좋아요/싫어요 연관관계 ---
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewReaction> reactions = new ArrayList<>();

    // --- 비즈니스 로직: 소프트 삭제 ---
    public void softDelete() {
        this.isDeleted = "Y";
        this.updatedAt = LocalDateTime.now();
        // 자식 답글들도 재귀적으로 소프트 삭제
        for (RestaurantReview child : children) {
            child.softDelete();
        }
    }
}