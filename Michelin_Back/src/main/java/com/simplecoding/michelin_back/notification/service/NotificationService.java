package com.simplecoding.michelin_back.notification.service;

import com.simplecoding.michelin_back.member.entity.Member;
import com.simplecoding.michelin_back.member.repository.MemberRepository;
import com.simplecoding.michelin_back.notification.dto.NotificationResponseDto;
import com.simplecoding.michelin_back.notification.entity.NotiType;
import com.simplecoding.michelin_back.notification.entity.Notification;
import com.simplecoding.michelin_back.notification.repository.NotificationRepository;
import com.simplecoding.michelin_back.notification.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final SseEmitters sseEmitters;

    /**
     * 1. 알림 생성 및 실시간 SSE 전송
     */
    @Transactional
    public void createNotification(Long memberId, Long senderId, Long relatedId,
                                   String title, String message,
                                   NotiType notiType, String targetUrl) {
        Member receiver = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        Member sender = (senderId != null)
                ? memberRepository.findById(senderId).orElse(null)
                : null;

        Notification notification = Notification.builder()
                .member(receiver)
                .sender(sender)
                .relatedId(relatedId)
                .title(title)
                .message(message)
                .notiType(notiType)
                .targetUrl(targetUrl)
                .build();

        notificationRepository.save(notification);

        // SSE 실시간 전송
        sseEmitters.send(memberId, NotificationResponseDto.from(notification));
    }

    /**
     * 2. Top 30 알림 목록 조회 (정렬: 미읽음 우선 → 최신순)
     */
    public List<NotificationResponseDto> getNotifications(Long memberId) {
        return notificationRepository
                .findTop30ByMember_MemberIdOrderByIsReadAscCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 3. 무한스크롤 페이징 조회 (5개씩)
     */
    public List<NotificationResponseDto> getNotificationsPaged(Long memberId, int page) {
        return notificationRepository
                .findNotificationsByMemberId(memberId, PageRequest.of(page, 5))
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 4. 미읽음 알림 개수 조회 (배지용)
     */
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countByMember_MemberIdAndIsRead(memberId, "N");
    }

    /**
     * 5. SSE 초기 연결 시 미읽음 알림 목록 조회
     */
    public List<NotificationResponseDto> getUnreadNotifications(Long memberId) {
        return notificationRepository
                .findByMember_MemberIdAndIsRead(memberId, "N")
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 6. 개별 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notiId, Long memberId) {
        notificationRepository.markAsRead(notiId, memberId);
    }

    /**
     * 7. 일괄 읽음 처리 (모두 읽음)
     */
    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    /**
     * 8. 개별 알림 삭제 (X 버튼)
     */
    @Transactional
    public void deleteNotification(Long notiId, Long memberId) {
        notificationRepository.deleteByNotiIdAndMember_MemberId(notiId, memberId);
    }

    /**
     * 9. 전체 알림 삭제 (모두 삭제)
     */
    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteByMember_MemberId(memberId);
    }

    /**
     * 10. 스케줄러 - 매일 새벽 4시 자동 삭제
     * 읽은 알림(Y): 14일 경과 시 삭제
     * 안 읽은 알림(N): 7일 경과 시 삭제
     */
    @Scheduled(cron = "0 00 4 * * *")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime readCutoff = LocalDateTime.now().minusDays(7);    // 읽은 알림 7일
        LocalDateTime unreadCutoff = LocalDateTime.now().minusDays(14); // 안 읽은 알림 14일
        notificationRepository.deleteOldNotifications(readCutoff, unreadCutoff);
    }

// --- [P5 담당자] NotiType 기반 도메인별 전용 메서드 ---

    /**
     * [1. 좋아요 알림]
     */
    @Transactional
    public void sendLikeAlert(Member receiver, Member sender, Long boardId) {
        if (receiver.getMemberId().equals(sender.getMemberId())) return;

        this.createNotification(
                receiver.getMemberId(),
                sender.getMemberId(),
                boardId,
                "❤️ 좋아요 알림",
                String.format("%s님이 내 게시글을 좋아합니다.", sender.getName()),
                NotiType.LIKE,
                "/review/detail/" + boardId
        );
    }

    /**
     * [2. 북마크 알림]
     */
    @Transactional
    public void sendBookmarkAlert(Member receiver, Member sender, Long boardId) {
        if (receiver.getMemberId().equals(sender.getMemberId())) return;

        this.createNotification(
                receiver.getMemberId(),
                sender.getMemberId(),
                boardId,
                "🔖 북마크 알림",
                String.format("%s님이 내 게시글을 북마크했습니다.", sender.getName()),
                NotiType.BOOKMARK,
                "/review/detail/" + boardId
        );
    }

    /**
     * [3. 원댓글 알림] - 게시글 작성자에게 전송
     */
    @Transactional
    public void sendCommentAlert(Member receiver, Member sender, Long boardId) {
        if (receiver.getMemberId().equals(sender.getMemberId())) return;

        this.createNotification(
                receiver.getMemberId(),
                sender.getMemberId(),
                boardId,
                "💬 새로운 댓글",
                String.format("%s님이 내 게시글에 댓글을 남겼습니다.", sender.getName()),
                NotiType.COMMENT,
                "/review/detail/" + boardId
        );
    }

    /**
     * [4. 답글 알림] - 원댓글 작성자에게 전송
     */
    @Transactional
    public void sendReplyAlert(Member receiver, Member sender, Long boardId) {
        if (receiver.getMemberId().equals(sender.getMemberId())) return;

        this.createNotification(
                receiver.getMemberId(),
                sender.getMemberId(),
                boardId,
                "↪️ 새로운 답글",
                String.format("%s님이 내 댓글에 답글을 남겼습니다.", sender.getName()),
                NotiType.REPLY,
                "/review/detail/" + boardId
        );
    }

    /**
     * [5. 시스템 공지 알림] - 특정 유저에게 시스템 메시지 전송
     */
    @Transactional
    public void sendSystemAlert(Member receiver, String title, String content, String url) {
        this.createNotification(
                receiver.getMemberId(),
                null, // 시스템은 발신자가 없음
                null,
                "📢 " + title,
                content,
                NotiType.SYSTEM,
                url != null ? url : "/home"
        );
    }
}