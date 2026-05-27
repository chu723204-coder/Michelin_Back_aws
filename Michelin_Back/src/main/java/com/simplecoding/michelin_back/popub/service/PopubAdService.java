package com.simplecoding.michelin_back.popub.service;

import com.simplecoding.michelin_back.popub.dto.PopupAdRequest;
import com.simplecoding.michelin_back.popub.dto.PopupAdResponse;
import com.simplecoding.michelin_back.popub.entity.PopupAd;
import com.simplecoding.michelin_back.popub.repository.PopubAdRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화
public class PopubAdService {

    private final PopubAdRepository popubAdRepository;

    // 생성자 주입
    public PopubAdService(PopubAdRepository popubAdRepository) {
        this.popubAdRepository = popubAdRepository;
    }

    /**
     * 1. [메인페이지용] 현재 노출 가능한 활성 광고 1건 조회
     */
    public PopupAdResponse getActivePopupAd() {
        return popubAdRepository.findActivePopupAd()
                .map(PopupAdResponse::new)
                .orElse(null); // 노출할 광고가 없으면 프론트에 null 반환 (리액트가 팝업 안 띄움)
    }

    /**
     * 2. [어드민용] 전체 광고 목록 조회 (최신등록순)
     */
    public List<PopupAdResponse> getAllPopupAds() {
        return popubAdRepository.findAll().stream()
                .map(PopupAdResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. [어드민용] 새로운 팝업 광고 등록 (이미지 파일 업로드 포함)
     */
    @Transactional
    public PopupAdResponse createPopupAd(PopupAdRequest request) {
        // 💡 파일 관련 로직 전부 삭제
        // 💡 request DTO에 imageUrl(URL 문자열)이 이미 담겨서 들어옵니다.

        PopupAd popupAd = request.toEntity();
        PopupAd savedAd = popubAdRepository.save(popupAd);

        return new PopupAdResponse(savedAd);
    }

    /**
     * 4. [어드민용] 팝업 광고 수정
     */
    @Transactional
    public PopupAdResponse updatePopupAd(Long adId, PopupAdRequest request) {
        PopupAd popupAd = popubAdRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 광고 번호입니다. ID: " + adId));

        // 데이터 수정 변경 감지(Dirty Checking) 활용
        popupAd.setTitle(request.getTitle());
        if (request.getImageUrl() != null) {
            popupAd.setImageUrl(request.getImageUrl());
        }
        popupAd.setLandingUrl(request.getLandingUrl());
        popupAd.setStartDate(request.getStartDate());
        popupAd.setEndDate(request.getEndDate());
        popupAd.setIsActive(request.getIsActive());

        return new PopupAdResponse(popupAd);
    }

    /**
     * 5. [어드민용] 팝업 광고 하드 삭제 (DB 데이터 시원하게 삭제)
     */
    @Transactional
    public void deletePopupAd(Long adId) {
        PopupAd popupAd = popubAdRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 광고 번호입니다. ID: " + adId));

        // 📂 파일 삭제 로직을 통째로 삭제하세요!

        // 🗄️ 오라클 DB 데이터 삭제만 남깁니다.
        popubAdRepository.delete(popupAd);
    }
}