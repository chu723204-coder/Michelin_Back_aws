package com.simplecoding.michelin_back.popub.dto;

import com.simplecoding.michelin_back.popub.entity.PopupAd; // 💡 정확한 엔티티 경로 반영!
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class PopupAdRequest {

    private String title;
    private String imageUrl;
    private String landingUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String isActive;

    // DTO에 담긴 데이터를 JPA 엔티티 객체로 변환해 주는 편의 메서드
    public PopupAd toEntity() {
        PopupAd popupAd = new PopupAd();
        popupAd.setTitle(this.title);
        popupAd.setImageUrl(this.imageUrl);
        popupAd.setLandingUrl(this.landingUrl);
        popupAd.setStartDate(this.startDate);
        popupAd.setEndDate(this.endDate);
        if (this.isActive != null) {
            popupAd.setIsActive(this.isActive);
        }
        return popupAd;
    }
}