package com.simplecoding.michelin_back.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // ✅ 빌더 패턴 사용 (객체 생성 시 매우 편리)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Integer totalPages;
    private final Integer totalElements;

    // 생성자를 private으로 두어 강제로 success() 메서드만 사용하게 함
    private ApiResponse(boolean success, String message, T data, Integer totalPages, Integer totalElements) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    // 1. 기존 성공 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "success", data, null, null);
    }

    // 2. 페이징 포함 성공
    public static <T> ApiResponse<T> success(T data, int totalPages, int totalElements) {
        return new ApiResponse<>(true, "success", data, totalPages, totalElements);
    }

    // 3. 메시지만 반환 성공
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, null);
    }

    // 4. 실패
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null, null, null);
    }
}