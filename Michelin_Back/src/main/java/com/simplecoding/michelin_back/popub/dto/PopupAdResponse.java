package com.simplecoding.michelin_back.popub.dto;

import com.simplecoding.michelin_back.popub.entity.PopupAd; // 💡 정확한 엔티티 경로 반영!
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PopupAdResponse {

    private Long adId;
    private String title;
    private String imageUrl;
    private String landingUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String isActive;
    private LocalDateTime regDate;

    // 엔티티(Entity) 객체를 받아서 DTO로 조립해 주는 생성자
    public PopupAdResponse(PopupAd popupAd) {
        this.adId = popupAd.getAdId();
        this.title = popupAd.getTitle();
        this.imageUrl = popupAd.getImageUrl();
        this.landingUrl = popupAd.getLandingUrl();
        this.startDate = popupAd.getStartDate();
        this.endDate = popupAd.getEndDate();
        this.isActive = popupAd.getIsActive();
        this.regDate = popupAd.getRegDate();
    }
}