package com.simplecoding.michelin_back.auth.service;

import com.simplecoding.michelin_back.auth.dto.AuthDto;
import com.simplecoding.michelin_back.common.CommonException;
import com.simplecoding.michelin_back.common.jwt.JwtTokenProvider;
import com.simplecoding.michelin_back.member.entity.Member;
import com.simplecoding.michelin_back.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** 일반 로그인 */
    @Transactional(readOnly = true)
    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        Member member = memberRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> CommonException.badRequest("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), member.getLoginPw())) {
            throw CommonException.badRequest("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if ("SUSPENDED".equals(member.getStatus())) {
            throw new CommonException(HttpStatus.FORBIDDEN,
                    "정지된 계정입니다. " + member.getSuspendedUntil() + "까지 이용 불가합니다.");
        }

        return AuthDto.LoginResponse.builder()
                .accessToken(jwtTokenProvider.createAccessToken(member))
                .tokenType("Bearer")
                .memberGrade(member.getMemberGrade())
                .build();
    }

    /** 회원가입 */
    @Transactional
    public void join(AuthDto.JoinRequest req) {
        if (memberRepository.existsByLoginId(req.getLoginId())) {
            throw new CommonException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new CommonException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .loginId(req.getLoginId())
                .loginPw(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .name(req.getName())
                .phone(req.getPhone())
                .build();

        memberRepository.save(member);
    }

    /** 아이디 중복 확인 — true: 사용 가능, false: 중복 */
    @Transactional(readOnly = true)
    public boolean checkLoginId(String loginId) {
        return !memberRepository.existsByLoginId(loginId);
    }
}
