package com.simplecoding.michelin_back.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitters {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long memberId, SseEmitter emitter) {
        this.emitters.put(memberId, emitter);
        log.info("📡 SSE 연결 성공 - memberId: {}, 현재 접속자 수: {}", memberId, emitters.size());

        emitter.onCompletion(() -> {
            log.info("🔌 SSE 연결 종료 (Completion) - memberId: {}", memberId);
            this.emitters.remove(memberId);
        });
        emitter.onTimeout(() -> {
            log.info("⏰ SSE 연결 타임아웃 - memberId: {}", memberId);
            this.emitters.remove(memberId);
        });
        emitter.onError((e) -> {
            log.error("❌ SSE 오류 - memberId: {}", memberId);
            this.emitters.remove(memberId);
        });
        return emitter;
    }

    // 1. 특정 개인에게 알림
    public void send(Long memberId, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            // 이 로그가 찍혀야 실시간 전송 시도가 시작된 겁니다!
            log.info("🚀 실시간 알림 전송 시도 - 수신자: {}", memberId);
            sendEvent(memberId, emitter, "notification", data);
        } else {
            // 접속 중이 아닐 때 로그를 남기면 디버깅이 편합니다.
            log.warn("⚠️ 실시간 알림 전송 실패 - memberId: {} 사용자가 접속 중이 아닙니다.", memberId);
        }
    }

    // 2. 브로드캐스팅
    public void broadcast(String eventName, Object data) {
        log.info("📢 SSE 브로드캐스트 시작 - 이벤트: {}, 접속 유저 수: {}", eventName, emitters.size());
        emitters.forEach((id, emitter) -> {
            sendEvent(id, emitter, eventName, data);
        });
    }

    // 전송 로직 공통화
    private void sendEvent(Long memberId, SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            // 최종 전송 성공 로그
            log.info("✅ SSE 전송 완료 - memberId: {}, 이벤트: {}", memberId, eventName);
        } catch (IOException e) {
            emitters.remove(memberId);
            log.error("❌ SSE 전송 실패 (memberId: {}): {}", memberId, e.getMessage());
        }
    }
}