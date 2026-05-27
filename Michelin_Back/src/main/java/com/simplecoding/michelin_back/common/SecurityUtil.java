package com.simplecoding.michelin_back.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    // 현재 로그인한 사용자 이메일 가져오기
    public static String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CommonException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }
        return authentication.getName();
    }
}