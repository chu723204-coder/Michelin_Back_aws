package com.simplecoding.michelin_back.auth.controller;

import com.simplecoding.michelin_back.auth.dto.AuthDto;
import com.simplecoding.michelin_back.auth.service.EmailService;
import com.simplecoding.michelin_back.auth.service.MemberService;
import com.simplecoding.michelin_back.common.ApiResponse;
import com.simplecoding.michelin_back.common.CommonException;
import com.simplecoding.michelin_back.common.jwt.JwtTokenProvider;
import com.simplecoding.michelin_back.member.entity.Member;
import com.simplecoding.michelin_back.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final EmailService emailService;

    /** 일반 로그인 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @RequestBody AuthDto.LoginRequest req,
            HttpServletResponse response) {

        AuthDto.LoginResponse loginResponse = memberService.login(req);

        // Refresh Token HttpOnly 쿠키 발급 (OAuth2와 동일 방식)
        String refreshToken = jwtTokenProvider.createRefreshToken(req.getLoginId());
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    /** 회원가입 */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> join(@RequestBody AuthDto.JoinRequest req) {
        memberService.join(req);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    /** 아이디 중복 확인 */
    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse<AuthDto.CheckIdResponse>> checkId(
            @RequestParam String loginId) {
        boolean available = memberService.checkLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success(
                AuthDto.CheckIdResponse.builder().available(available).build()));
    }

    /** 이메일 인증코드 발송 */
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(
            @RequestBody AuthDto.EmailSendRequest req) {
        emailService.sendCode(req.getEmail());
        return ResponseEntity.ok(ApiResponse.success("인증코드가 발송되었습니다."));
    }

    /** 이메일 인증코드 확인 */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailCode(
            @RequestBody AuthDto.EmailVerifyRequest req) {
        emailService.verifyCode(req.getEmail(), req.getCode());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다."));
    }

    /** Refresh Token(쿠키) → 새 Access Token 발급 */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw CommonException.unauthorized("유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
        }

        String loginId = jwtTokenProvider.getLoginId(refreshToken);
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> CommonException.notFound("회원을 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(member);

        return ResponseEntity.ok(ApiResponse.success(
                AuthDto.TokenResponse.builder()
                        .accessToken(newAccessToken)
                        .tokenType("Bearer")
                        .build()
        ));
    }

    /** 로그아웃 — Refresh Token 쿠키 만료 처리 */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setMaxAge(0);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        response.addCookie(expiredCookie);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
