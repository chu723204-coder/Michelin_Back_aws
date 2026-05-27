package com.simplecoding.michelin_back.popub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "POPUP_AD") // 오라클 DB의 POPUP_AD 테이블과 매핑
@Getter @Setter           // 롬복(Lombok)으로 Getter, Setter 자동 생성
public class PopupAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // GENERATED ALWAYS AS IDENTITY와 매핑
    @Column(name = "AD_ID")
    private Long adId;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "LANDING_URL", length = 500)
    private String landingUrl;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    // 오라클의 CHAR(1) CHECK (IN ('Y', 'N'))에 대응
    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    private String isActive = "Y"; // 기본값 'Y' 설정

    @Column(name = "REG_DATE", updatable = false)
    private LocalDateTime regDate;

    // DB에 인서트(INSERT)되기 전에 현재 시간으로 등록일을 자동으로 채워주는 메서드
    @PrePersist
    protected void onCreate() {
        this.regDate = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = "Y";
        }
    }
}