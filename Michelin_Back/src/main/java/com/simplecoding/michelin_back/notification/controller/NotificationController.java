package com.simplecoding.michelin_back.notification.controller;

import com.simplecoding.michelin_back.notification.dto.NotificationResponseDto;
import com.simplecoding.michelin_back.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림 API", description = "사용자 실시간 알림 조회 및 읽음 상태 관리 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회 (Top 30)", description = "특정 회원의 알림 목록을 미읽음 우선, 최신순으로 최대 30개 반환합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(@PathVariable Long memberId) {
        return ResponseEntity.ok(notificationService.getNotifications(memberId));
    }

    @Operation(summary = "알림 무한스크롤 페이징 조회", description = "5개씩 페이징하여 반환합니다. page=0부터 시작합니다.")
    @GetMapping("/{memberId}/paged")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsPaged(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(notificationService.getNotificationsPaged(memberId, page));
    }

    @Operation(summary = "안 읽은 알림 개수 조회", description = "읽지 않은 알림(isRead = 'N')의 총 개수를 반환합니다. (배지 표시용)")
    @GetMapping("/{memberId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long memberId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(memberId));
    }

    @Operation(summary = "특정 알림 읽음 처리", description = "알림 클릭 시 해당 알림을 읽음 상태('Y')로 변경합니다.")
    @PatchMapping("/{memberId}/{notiId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long memberId,
            @PathVariable Long notiId) {
        notificationService.markAsRead(notiId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "해당 회원의 모든 안 읽은 알림을 일괄 읽음 처리합니다.")
    @PatchMapping("/{memberId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long memberId) {
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "개별 알림 삭제", description = "특정 알림을 삭제합니다. (X 버튼)")
    @DeleteMapping("/{memberId}/{notiId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long memberId,
            @PathVariable Long notiId) {
        notificationService.deleteNotification(notiId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 알림 삭제", description = "해당 회원의 모든 알림을 삭제합니다.")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable Long memberId) {
        notificationService.deleteAllNotifications(memberId);
        return ResponseEntity.ok().build();
    }
}