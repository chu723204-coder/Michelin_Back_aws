package com.simplecoding.michelin_back.popub.controller;

import com.simplecoding.michelin_back.popub.dto.PopupAdRequest;
import com.simplecoding.michelin_back.popub.dto.PopupAdResponse;
import com.simplecoding.michelin_back.popub.service.PopubAdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ads") // 💡 기본 공통 URL 주소 설정
public class PopubAdController {

    private final PopubAdService popubAdService;

    // 생성자 주입
    public PopubAdController(PopubAdService popubAdService) {
        this.popubAdService = popubAdService;
    }

    /**
     * 1. [메인페이지용] 현재 노출 기간에 맞는 활성 광고 딱 1건 조회
     * 요청 주소: GET http://localhost:8080/api/v1/ads/active
     */
    @GetMapping("/active")
    public ResponseEntity<PopupAdResponse> getActivePopupAd() {
        PopupAdResponse response = popubAdService.getActivePopupAd();
        // 만약 노출할 광고가 없으면 204 No Content를 보내거나 null을 200 OK로 반환
        return ResponseEntity.ok(response);
    }

    /**
     * 2. [어드민용] 전체 광고 리스트 조회
     * 요청 주소: GET http://localhost:8080/api/v1/ads
     */
    @GetMapping
    public ResponseEntity<List<PopupAdResponse>> getAllPopupAds() {
        List<PopupAdResponse> list = popubAdService.getAllPopupAds();
        return ResponseEntity.ok(list);
    }

    /**
     * 3. [어드민용] 새로운 팝업 광고 등록 (이미지 파일 + 텍스트 데이터 복합 전송)
     * 요청 주소: POST http://localhost:8080/api/v1/ads
     * 주의: 파일이 포함되므로 JSON이 아닌 @ModelAttribute 형식을 주로 사용합니다.
     */
    @PostMapping
    public ResponseEntity<PopupAdResponse> createPopupAd(@RequestBody PopupAdRequest request) {
        // 💡 이미지 파일(file)은 이제 안 받으니까 제거합니다.
        // 💡 서비스 메서드도 그에 맞춰서 file 파라미터가 없는 것으로 호출하세요.
        PopupAdResponse response = popubAdService.createPopupAd(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 4. [어드민용] 기존 팝업 광고 내용 수정
     * 요청 주소: PUT http://localhost:8080/api/v1/ads/{adId}
     */
    @PutMapping("/{adId}")
    public ResponseEntity<PopupAdResponse> updatePopupAd(
            @PathVariable("adId") Long adId,
            @RequestBody PopupAdRequest request) {

        PopupAdResponse response = popubAdService.updatePopupAd(adId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. [어드민용] 팝업 광고 삭제 (하드 삭제)
     * 요청 주소: DELETE http://localhost:8080/api/v1/ads/{adId}
     */
    @DeleteMapping("/{adId}")
    public ResponseEntity<Void> deletePopupAd(@PathVariable("adId") Long adId) {
        popubAdService.deletePopupAd(adId);
        return ResponseEntity.noContent().build(); // 204 No Content 리턴
    }
}