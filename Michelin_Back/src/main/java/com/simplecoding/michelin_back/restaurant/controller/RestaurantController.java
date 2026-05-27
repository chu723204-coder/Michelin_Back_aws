package com.simplecoding.michelin_back.restaurant.controller;

import com.simplecoding.michelin_back.common.ApiResponse;
import com.simplecoding.michelin_back.common.MarkerDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantRequestDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantResponseDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantSearchDto;
import com.simplecoding.michelin_back.restaurant.entity.RestaurantImage;
import com.simplecoding.michelin_back.restaurant.service.RestaurantImageService;
import com.simplecoding.michelin_back.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantImageService restaurantImageService;

    // ── P2 - 식당 관련 API ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantResponseDto>>> getList(RestaurantSearchDto searchDto) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getList(searchDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getDetail(id)));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> create(
            @RequestPart("requestDto") RestaurantRequestDto requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.create(requestDto, files)));
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> update(
            @PathVariable Long id,
            @RequestPart("requestDto") RestaurantRequestDto requestDto,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            @RequestPart(value = "deleteImageIds", required = false) List<Long> deleteImageIds) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.update(id, requestDto, newFiles, deleteImageIds)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("음식점이 삭제되었습니다."));
    }

    // ── 이미지 관련 API ──────────────────────────────────────────

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(restaurantImageService.uploadImage(id, file, isMain)));
    }

    // ✅ [STEP 5] {id} 파라미터 추가 - 미사용 path variable 정리
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ApiResponse<String>> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        log.info("[이미지 삭제] restaurantId={}, imageId={}", id, imageId);
        restaurantImageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("이미지가 삭제되었습니다."));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getImages(@PathVariable Long id) {
        List<Map<String, Object>> result = restaurantImageService.getImages(id).stream()
                .map(img -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", img.getId());
                    map.put("imageUrl", img.getImageUrl());
                    map.put("isMain", img.getIsMain());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── 마커 / 검색 / 자동완성 API ───────────────────────────────
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> getAutocomplete(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getAutocomplete(keyword)));
    }

    @GetMapping("/markers")
    public ResponseEntity<List<MarkerDto>> getMarkers(
            @RequestParam(name = "lat") Double lat,
            @RequestParam(name = "lng") Double lng) {
        return ResponseEntity.ok(restaurantService.getRestaurantMarkers(lat, lng));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MarkerDto>> search(@RequestParam("name") String name) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(name));
    }
}