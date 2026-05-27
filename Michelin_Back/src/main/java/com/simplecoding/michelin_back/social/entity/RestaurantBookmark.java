package com.simplecoding.michelin_back.social.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESTAURANT_BOOKMARKS")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RestaurantBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOOKMARK_SEQ")
    @SequenceGenerator(name = "BOOKMARK_SEQ", sequenceName = "SEQ_BOOKMARK_ID", allocationSize = 1)
    private Long id;

    @Column(name = "MEMBER_ID") // [수정] USER_ID -> MEMBER_ID
    private Long memberId;

    @Column(name = "RESTAURANT_ID")
    private Long restaurantId;

    @Column(name = "FOLDER_NAME")
    private String folderName;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}