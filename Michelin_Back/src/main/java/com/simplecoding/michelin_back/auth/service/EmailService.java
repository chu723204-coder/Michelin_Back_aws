package com.simplecoding.michelin_back.auth.service;

import com.simplecoding.michelin_back.common.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "email:verify:";
    private static final long TTL_SECONDS = 180; // 3분

    /** 이메일로 6자리 인증코드 발송 후 Redis에 저장 */
    public void sendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        redisTemplate.opsForValue().set(PREFIX + email, code, Duration.ofSeconds(TTL_SECONDS));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[THE PLATE] 이메일 인증코드");
        message.setText("인증코드: " + code + "\n\n3분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    /** 인증코드 확인 — 맞으면 Redis에서 삭제 */
    public void verifyCode(String email, String code) {
        String stored = redisTemplate.opsForValue().get(PREFIX + email);
        if (stored == null) {
            throw CommonException.badRequest("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!stored.equals(code)) {
            throw CommonException.badRequest("인증코드가 올바르지 않습니다.");
        }
        redisTemplate.delete(PREFIX + email);
    }
}
