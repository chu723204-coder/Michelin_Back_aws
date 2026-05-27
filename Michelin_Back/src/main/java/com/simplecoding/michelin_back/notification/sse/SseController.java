package com.simplecoding.michelin_back.notification.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitters sseEmitters;

    /**
     * SSE 연결 생성
     * @param memberId 연결할 회원 ID
     * @return SseEmitter
     */
    @GetMapping(value = "/subscribe/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long memberId) {
        // 1. 타임아웃 설정 (30분).
        // 서버 설정에 따라 다르지만 너무 길면 리소스 점유가 커질 수 있으니 적절히 조절하세요.
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 2. 초기 연결 시 리스트에 먼저 등록
        sseEmitters.add(memberId, emitter);

        // 3. 최초 연결 시 dummy 데이터를 전송해야 503 Service Unavailable 에러를 방지할 수 있습니다.
        try {
            emitter.send(SseEmitter.event()
                    .name("connect") // 리액트에서 eventSource.addEventListener("connect", ...)로 수신
                    .data("connected! [memberId=" + memberId + "]"));
        } catch (IOException e) {
            log.error("초기 SSE 연결 데이터 전송 실패 : {}", e.getMessage());
            // 전송 실패 시 리스트에서 제거
        }

        // 4. Emitter가 만료되거나 에러가 났을 때의 처리 (중복 방지 로직은 SseEmitters 내부에 이미 있음)
        emitter.onCompletion(() -> log.info("SSE 연결 종료: memberId={}", memberId));
        emitter.onTimeout(() -> log.warn("SSE 연결 타임아웃: memberId={}", memberId));
        emitter.onError((e) -> log.error("SSE 연결 에러: memberId={}, message={}", memberId, e.getMessage()));

        return emitter;
    }
}