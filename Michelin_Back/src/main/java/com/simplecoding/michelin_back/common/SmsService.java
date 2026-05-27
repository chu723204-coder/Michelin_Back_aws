package com.simplecoding.michelin_back.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    private DefaultMessageService messageService;

    @Value("${solapi.api-key:}")
    private String apiKey;

    @Value("${solapi.api-secret:}")
    private String apiSecret;

    @Value("${solapi.from-number:}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            log.warn("[SmsService] solapi 키가 설정되지 않았습니다. SMS 기능이 비활성화됩니다.");
            return;
        }
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
        log.info("[SmsService] 솔라피 SMS 서비스 초기화 완료");
    }

    /** 패널티 알림 문자 */
    public void sendPenaltyMessage(String to, String name, String reason, String until) {
        if (messageService == null) {
            log.warn("[SmsService] SMS 미설정 — 발송 건너뜀 (to: {})", to);
            return;
        }
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to.replaceAll("[^0-9]", ""));
        message.setText("[미슐랭] " + name + "님, " + reason + " 사유로 " + until + "까지 서비스 이용이 제한됩니다.");
        messageService.sendOne(new SingleMessageSendingRequest(message));
    }

    /** 인증번호 문자 (확장용) */
    public void sendVerificationCode(String to, String code) {
        if (messageService == null) {
            log.warn("[SmsService] SMS 미설정 — 발송 건너뜀 (to: {})", to);
            return;
        }
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to.replaceAll("[^0-9]", ""));
        message.setText("[미슐랭] 인증번호: " + code + " (5분 이내 입력해주세요)");
        messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
