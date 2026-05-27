package com.simplecoding.michelin_back.restaurant.service;

import com.simplecoding.michelin_back.common.CommonException;
import com.simplecoding.michelin_back.restaurant.entity.Restaurant;
import com.simplecoding.michelin_back.restaurant.entity.RestaurantImage;
import com.simplecoding.michelin_back.restaurant.repository.RestaurantImageRepository;
import com.simplecoding.michelin_back.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantImageService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    // ✅ 기본값 추가 - local 프로파일 없어도 기동 가능
    @Value("${image.restaurant-dir:C:/images/restaurants/}")
    private String restaurantDir;

    @Value("${image.base-url:http://localhost:8080/images/}")
    private String baseUrl;

    // 이미지 업로드
    @Transactional
    public String uploadImage(Long restaurantId, MultipartFile file, boolean isMain) throws IOException {

        // 음식점 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CommonException(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."));

        // 저장 폴더 생성
        File dir = new File(restaurantDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("[이미지] 폴더 생성 완료 : {}", restaurantDir);
        }

        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String savedFilename = UUID.randomUUID().toString() + ext;
        String savedPath = restaurantDir + savedFilename;

        // 파일 저장
        file.transferTo(new File(savedPath));
        log.info("[이미지] 파일 저장 완료 : {}", savedPath);

        // 대표 이미지로 설정할 경우 기존 대표 이미지 해제
        if (isMain) {
            List<RestaurantImage> existingImages =
                    restaurantImageRepository.findByRestaurantId(restaurantId);
            existingImages.forEach(img -> img.updateIsMain("N"));
        }

        // DB 저장
        String imageUrl = baseUrl + "restaurants/" + savedFilename;
        RestaurantImage image = RestaurantImage.builder()
                .restaurant(restaurant)
                .imageUrl(imageUrl)
                .isMain(isMain ? "Y" : "N")
                .sortOrder(0)
                .build();
        restaurantImageRepository.save(image);
        log.info("[이미지] DB 저장 완료 : {}", imageUrl);

        return imageUrl;
    }

    // 이미지 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        RestaurantImage image = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new CommonException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."));

        // 실제 파일 삭제
        String imageUrl = image.getImageUrl();
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        File file = new File(restaurantDir + filename);
        if (file.exists()) {
            file.delete();
            log.info("[이미지] 파일 삭제 완료 : {}", filename);
        }

        // DB 삭제
        restaurantImageRepository.delete(image);
        log.info("[이미지] DB 삭제 완료 : imageId={}", imageId);
    }

    // 음식점 이미지 목록 조회
    public List<RestaurantImage> getImages(Long restaurantId) {
        return restaurantImageRepository.findByRestaurantId(restaurantId);
    }
}