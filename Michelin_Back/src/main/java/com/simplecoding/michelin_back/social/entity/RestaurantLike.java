package com.simplecoding.michelin_back.social.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESTAURANT_LIKES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantLike {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LIKE_SEQ")
    @SequenceGenerator(name = "LIKE_SEQ", sequenceName = "SEQ_LIKE_ID", allocationSize = 1)
    private Long id;

    @Column(name = "MEMBER_ID") // [수정] 컬럼명 일치
    private Long memberId;

    @Column(name = "RESTAURANT_ID")
    private Long restaurantId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

