package com.simplecoding.michelin_back.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * DB 컬럼: INSERT_TIME / UPDATE_TIME (BaseTimeEntity 미사용 — 기존 DB 컬럼명 상이)
 */
@Entity
@Table(name = "MEMBER")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_member")
    @SequenceGenerator(name = "seq_member", sequenceName = "SEQ_MEMBER", allocationSize = 1)
    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "LOGIN_ID", nullable = false, length = 50)
    private String loginId;

    @Column(name = "LOGIN_PW", nullable = false, length = 255)
    private String loginPw;

    @Column(name = "EMAIL", nullable = false, length = 255)
    private String email;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Builder.Default
    @Column(name = "MEMBER_GRADE", nullable = false, length = 2)
    private String memberGrade = "N";

    @Builder.Default
    @Column(name = "PROVIDER", length = 20)
    private String provider = "LOCAL";

    @Column(name = "PROVIDER_ID", length = 100)
    private String providerId;

    @Builder.Default
    @Column(name = "PENALTY_COUNT")
    private Integer penaltyCount = 0;

    @Column(name = "SUSPENDED_UNTIL")
    private LocalDate suspendedUntil;

    @CreatedDate
    @Column(name = "INSERT_TIME", updatable = false)
    private LocalDateTime insertTime;

    @LastModifiedDate
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;

    // ── 비즈니스 메서드 ───────────────────────────────────────

    /** 페널티 1회 추가 */
    public void addPenalty() {
        this.penaltyCount = (this.penaltyCount == null ? 0 : this.penaltyCount) + 1;
    }

    /** 정지 처리 */
    public void suspend(LocalDate until) {
        this.status = "SUSPENDED";
        this.suspendedUntil = until;
    }

    /** 정지 해제 */
    public void releaseSuspension() {
        this.status = "ACTIVE";
        this.suspendedUntil = null;
    }

    /** 등급 변경 (관리자 수동) */
    public void changeGrade(String grade) {
        this.memberGrade = grade;
    }
}
