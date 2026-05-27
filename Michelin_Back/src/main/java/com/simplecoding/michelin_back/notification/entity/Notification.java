package com.simplecoding.michelin_back.notification.entity;

import com.simplecoding.michelin_back.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION", indexes = {
        @Index(name = "idx_noti_member_read_created", columnList = "MEMBER_ID, IS_READ, CREATED_AT")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTI_SEQ_GEN")
    @SequenceGenerator(name = "NOTI_SEQ_GEN", sequenceName = "SEQ_NOTIFICATION_ID", allocationSize = 1)
    @Column(name = "NOTI_ID")
    private Long notiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID")  // Nullable
    private Member sender;

    @Column(name = "RELATED_ID")
    private Long relatedId;

    @Column(name = "TITLE", length = 100)
    private String title;

    @Column(name = "MESSAGE", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "NOTI_TYPE", nullable = false, length = 50)
    private NotiType notiType;

    @Column(name = "TARGET_URL", length = 255)
    private String targetUrl;

    @Builder.Default
    @ColumnDefault("'N'")
    @Column(name = "IS_READ", length = 1, nullable = false)
    private String isRead = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    public void markAsRead() {
        this.isRead = "Y";
    }

//    properties오류 제거
}