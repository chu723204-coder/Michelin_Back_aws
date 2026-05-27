package com.simplecoding.michelin_back.restaurant.service;

import com.simplecoding.michelin_back.common.CommonException;
import com.simplecoding.michelin_back.common.MarkerDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantRequestDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantResponseDto;
import com.simplecoding.michelin_back.restaurant.dto.RestaurantSearchDto;
import com.simplecoding.michelin_back.restaurant.entity.Restaurant;
import com.simplecoding.michelin_back.restaurant.entity.RestaurantImage;
import com.simplecoding.michelin_back.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageService restaurantImageService;

    // ── P2 - 음식점 목록 조회 (필터 + 페이지네이션 + 키워드) ────────────
    public Page<RestaurantResponseDto> getList(RestaurantSearchDto searchDto) {
        log.info("[목록 조회] keyword={}, grade={}, city={}, district={}, isGreenStar={}, page={}, size={}",
                searchDto.getKeyword(), searchDto.getGrade(), searchDto.getCity(),
                searchDto.getDistrict(), searchDto.getIsGreenStar(),
                searchDto.getPage(), searchDto.getSize());

        Pageable pageable = PageRequest.of(
                searchDto.getPage(),
                searchDto.getSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        // ✅ [STEP 2] ACTIVE 상태만 조회
        Specification<Restaurant> spec = (root, query, cb) ->
                cb.equal(root.get("status"), "ACTIVE");

        if (searchDto.getGrade() != null && !searchDto.getGrade().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("grade"), searchDto.getGrade()));
        }
        if (searchDto.getCity() != null && !searchDto.getCity().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("city"), searchDto.getCity()));
        }
        if (searchDto.getDistrict() != null && !searchDto.getDistrict().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("district"), searchDto.getDistrict()));
        }
        if (searchDto.getIsGreenStar() != null && !searchDto.getIsGreenStar().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isGreenStar"), searchDto.getIsGreenStar()));
        }
        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
            log.info("[키워드 필터 적용] keyword={}", searchDto.getKeyword());
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("restaurantName"), "%" + searchDto.getKeyword() + "%"));
        }

        Page<RestaurantResponseDto> result = restaurantRepository.findAll(spec, pageable)
                .map(RestaurantResponseDto::new);
        log.info("[목록 조회 완료] 총 {}건", result.getTotalElements());
        return result;
    }

    // ── P2 - 음식점 상세 조회 (viewCount +1) ────────────────────────────
    @Transactional
    public RestaurantResponseDto getDetail(Long id) {
        // 1. 음식점 조회 (이미지까지 한 번에 가져와야 합니다)
        // repository에서 findById로 이미지를 가져오지 못한다면,
        // 엔티티 매핑에서 @OneToMany(fetch = FetchType.EAGER) 설정을 확인하세요.
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new CommonException(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."));

        // 2. 조회수 증가
        restaurant.increaseViewCount();

        // 3. DTO 생성 (이미지 처리는 DTO 생성자 내부에서 이미 수행됨)
        return new RestaurantResponseDto(restaurant);
    }

    // ── P2 - 음식점 등록 (이미지 포함) ──────────────────────────────────
    @Transactional
    public RestaurantResponseDto create(RestaurantRequestDto requestDto, List<MultipartFile> files) throws IOException {
        Restaurant restaurant = Restaurant.builder()
                .restaurantName(requestDto.getRestaurantName())
                .grade(requestDto.getGrade())
                .city(requestDto.getCity())
                .district(requestDto.getDistrict())
                .lat(requestDto.getLat())
                .lng(requestDto.getLng())
                .isGreenStar(requestDto.getIsGreenStar())
                .address(requestDto.getAddress())
                .kakaoPlaceUrl(requestDto.getKakaoPlaceUrl())
                .kakaoPlaceId(requestDto.getKakaoPlaceId())
                .phone(requestDto.getPhone())
                .category(requestDto.getCategory())
                .status("ACTIVE")  // ✅ 이거 추가
                .build();

        restaurantRepository.save(restaurant);

        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                boolean isMain = (i == 0);
                restaurantImageService.uploadImage(restaurant.getId(), files.get(i), isMain);
            }
        }
        return new RestaurantResponseDto(restaurant);
    }

    // ── P2 - 음식점 수정 ──────────────────────────────────────────────────
    @Transactional
    public RestaurantResponseDto update(Long id, RestaurantRequestDto requestDto,
                                        List<MultipartFile> newFiles,
                                        List<Long> deleteImageIds) throws IOException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new CommonException(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."));

        restaurant.update(
                requestDto.getRestaurantName(),
                requestDto.getCity(),
                requestDto.getDistrict(),
                requestDto.getAddress(),
                requestDto.getPhone(),
                requestDto.getGrade(),
                requestDto.getIsGreenStar(),
                requestDto.getLat(),
                requestDto.getLng(),
                requestDto.getKakaoPlaceUrl(),
                requestDto.getKakaoPlaceId(),
                requestDto.getCategory()
        );

        if (deleteImageIds != null) {
            for (Long imageId : deleteImageIds) {
                restaurantImageService.deleteImage(imageId);
            }
        }

        if (newFiles != null) {
            for (MultipartFile file : newFiles) {
                restaurantImageService.uploadImage(restaurant.getId(), file, false);
            }
        }
        return new RestaurantResponseDto(restaurant);
    }

    // ── P2 - 음식점 삭제 (Soft Delete) ──────────────────────────────────
    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new CommonException(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."));
        restaurant.softDelete();
    }

    // ── P2 - 검색 자동완성 ───────────────────────────────────────────────
    // ✅ [STEP 2] ACTIVE 상태만 자동완성에 노출
    public List<RestaurantResponseDto> getAutocomplete(String keyword) {
        Pageable pageable = PageRequest.of(0, 10);
        return restaurantRepository.findAutocomplete(keyword, pageable)
                .stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }

    // ── P4 - 지도 마커 조회 (반경 내 음식점) ────────────────────────────
    // ✅ [STEP 2] ACTIVE 상태만 마커에 노출
    public List<MarkerDto> getRestaurantMarkers(Double lat, Double lng) {
        Double radius = 3.5;
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithinRadius(lat, lng, radius);
        log.info("[지도 마커] 중심 좌표 ({}, {}) 기준 {}건 조회 완료", lat, lng, restaurants.size());

        return restaurants.stream()
                .filter(res -> "ACTIVE".equals(res.getStatus()))
                .map(res -> {
                    String mainImageUrl = (res.getImages() != null) ? res.getImages().stream()
                            .filter(img -> "Y".equals(img.getIsMain()))
                            .map(RestaurantImage::getImageUrl)
                            .findFirst()
                            .orElse("default_image_url") : "default_image_url";

                    return MarkerDto.builder()
                            .id(res.getId())
                            .restaurantName(res.getRestaurantName())
                            .lat(res.getLat())
                            .lng(res.getLng())
                            .grade(res.getGrade())
                            .phone(res.getPhone())
                            .address(res.getAddress())
                            .category(res.getCategory())
                            .markerColor(determineMarkerColor(res.getGrade()))
                            .imageUrl(mainImageUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── P4 - 음식점 이름 검색 ────────────────────────────────────────────
    // ✅ [STEP 2] ACTIVE 상태만 검색 결과에 노출
    public List<MarkerDto> searchRestaurants(String name) {
        List<Restaurant> restaurants = restaurantRepository.findByRestaurantNameContainingIgnoreCase(name);

        return restaurants.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .map(r -> {
                    String color = "#feb2b2";
                    if ("3 Stars".equals(r.getGrade())) color = "#e62117";
                    else if ("2 Stars".equals(r.getGrade())) color = "#ff5e5e";

                    String imageUrl = (r.getImages() != null && !r.getImages().isEmpty())
                            ? r.getImages().get(0).getImageUrl()
                            : "default_image_url";

                    return MarkerDto.builder()
                            .id(r.getId())
                            .restaurantName(r.getRestaurantName())
                            .lat(r.getLat())
                            .lng(r.getLng())
                            .grade(r.getGrade())
                            .address(r.getAddress())
                            .category(r.getCategory())
                            .markerColor(color)
                            .imageUrl(imageUrl)
                            .build();
                }).collect(Collectors.toList());
    }

    // ── 공통 - 마커 색상 결정 ────────────────────────────────────────────
    private String determineMarkerColor(String grade) {
        if (grade == null) return "blue";
        if (grade.contains("3스타")) return "red";
        if (grade.contains("2스타")) return "orange";
        if (grade.contains("1스타")) return "yellow";
        if (grade.contains("빕 구르망")) return "green";
        return "blue";
    }
}