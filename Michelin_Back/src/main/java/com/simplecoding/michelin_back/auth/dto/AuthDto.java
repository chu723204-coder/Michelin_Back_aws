package com.simplecoding.michelin_back.auth.dto;

import lombok.*;

public class AuthDto {

    /** Refresh → 새 AccessToken 응답 */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
    }

    /** 일반 로그인 요청 */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String loginId;
        private String password;
    }

    /** 일반 로그인 응답 */
    @Getter
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String tokenType;
        private String memberGrade;
    }

    /** 회원가입 요청 */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        private String loginId;
        private String password;
        private String email;
        private String name;
        private String phone;
        private boolean marketingAgree;
    }

    /** 아이디 중복 확인 응답 */
    @Getter
    @Builder
    public static class CheckIdResponse {
        private boolean available;
    }

    /** 이메일 인증코드 발송 요청 */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailSendRequest {
        private String email;
    }

    /** 이메일 인증코드 확인 요청 */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerifyRequest {
        private String email;
        private String code;
    }
}
