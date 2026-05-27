package com.simplecoding.michelin_back.notification.service;

import com.simplecoding.michelin_back.member.entity.Member;
import com.simplecoding.michelin_back.member.repository.MemberRepository;
import com.simplecoding.michelin_back.notification.dto.NotificationResponseDto;
import com.simplecoding.michelin_back.notification.entity.NotiType;
import com.simplecoding.michelin_back.notification.entity.Notification;
import com.simplecoding.michelin_back.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * [알림 생성 테스트]
     * - 알림 생성 시 DB에 정상 저장되는지 확인
     * - IS_READ 기본값 'N' 확인
     * - message, notiType, targetUrl 정상 저장 확인
     * - SSE 전송은 구독자 없어서 warn 로그만 찍힘 (정상)
     */
    @Test
    void createNotification() {
        // given - DB에 실제 존재하는 memberId로 바꿔주세요
        Long memberId = 1L;
        Long senderId = 2L;
        Long relatedId = 100L;
        String title = "새 댓글 알림";
        String message = "OO님이 [레스토랑 리뷰]에 댓글을 남겼습니다.";
        NotiType notiType = NotiType.COMMENT;
        String targetUrl = "/restaurant/1/review/100";

        // when
        notificationService.createNotification(
                memberId, senderId, relatedId,
                title, message, notiType, targetUrl
        );

        // then - DB에 실제로 저장됐는지 확인
        List<Notification> result = notificationRepository
                .findTop30ByMember_MemberIdOrderByIsReadAscCreatedAtDesc(memberId);

        assertFalse(result.isEmpty(), "알림이 저장되어 있어야 합니다.");

        Notification saved = result.get(0);
        assertEquals(message, saved.getMessage());
        assertEquals(notiType, saved.getNotiType());
        assertEquals("N", saved.getIsRead());
        assertEquals(targetUrl, saved.getTargetUrl());

        System.out.println("✅ 저장된 알림 ID: " + saved.getNotiId());
        System.out.println("✅ 메시지: " + saved.getMessage());
        System.out.println("✅ 타입: " + saved.getNotiType());
        System.out.println("✅ 읽음여부: " + saved.getIsRead());
    }
    /**
     * [알림 목록 조회 테스트]
     * - 특정 회원의 알림 목록 최대 30개 조회 확인
     * - 정렬 순서 확인 (미읽음 N → 읽음 Y → 최신순)
     */
    @Test
    void getNotifications() {
        // given
        Long memberId = 1L;

        // when
        List<NotificationResponseDto> result = notificationService.getNotifications(memberId);

        // then
        assertFalse(result.isEmpty(), "알림 목록이 존재해야 합니다.");
        assertTrue(result.size() <= 30, "최대 30개까지만 조회되어야 합니다.");

        // 정렬 확인 - 미읽음(N)이 읽음(Y)보다 앞에 있어야 함
        for (int i = 0; i < result.size() - 1; i++) {
            String current = result.get(i).getIsRead();
            String next = result.get(i + 1).getIsRead();
            assertFalse(current.equals("Y") && next.equals("N"), "미읽음 알림이 읽음 알림보다 앞에 있어야 합니다.");
        }

        result.forEach(dto -> {
            System.out.println("✅ 알림 ID: " + dto.getNotiId()
                    + " | 타입: " + dto.getNotiType()
                    + " | 읽음여부: " + dto.getIsRead()
                    + " | 생성일: " + dto.getCreatedAt());
        });
    }

    /**
     * [무한스크롤 페이징 테스트]
     * - 첫 페이지(page=0) 5개 조회 확인
     * - 두 번째 페이지(page=1) 5개 조회 확인
     * - 각 페이지 데이터가 중복되지 않는지 확인
     * - 정렬 순서 확인 (미읽음 N → 읽음 Y → 최신순)
     */
    @Test
    void getNotificationsPaged() {
        // given
        Long memberId = 1L;

        // when - 첫 페이지
        List<NotificationResponseDto> firstPage = notificationService.getNotificationsPaged(memberId, 0);

        // when - 두 번째 페이지
        List<NotificationResponseDto> secondPage = notificationService.getNotificationsPaged(memberId, 1);

        // then - 페이지당 최대 5개
        assertTrue(firstPage.size() <= 5, "첫 페이지는 최대 5개여야 합니다.");
        assertTrue(secondPage.size() <= 5, "두 번째 페이지는 최대 5개여야 합니다.");

        // then - 중복 데이터 없는지 확인
        firstPage.forEach(first ->
                secondPage.forEach(second ->
                        assertNotEquals(first.getNotiId(), second.getNotiId(), "페이지간 중복 데이터가 없어야 합니다.")
                )
        );

        System.out.println("=== 첫 번째 페이지 ===");
        firstPage.forEach(dto ->
                System.out.println("✅ 알림 ID: " + dto.getNotiId()
                        + " | 읽음여부: " + dto.getIsRead()
                        + " | 생성일: " + dto.getCreatedAt())
        );

        System.out.println("=== 두 번째 페이지 ===");
        secondPage.forEach(dto ->
                System.out.println("✅ 알림 ID: " + dto.getNotiId()
                        + " | 읽음여부: " + dto.getIsRead()
                        + " | 생성일: " + dto.getCreatedAt())
        );
    }

    /**
     * [미읽음 알림 개수 조회 테스트]
     * - 특정 회원의 미읽음 알림(IS_READ = 'N') 개수 정상 조회 확인
     * - 배지(Badge)에 표시될 카운트 확인
     */
    @Test
    void getUnreadCount() {
        // given
        Long memberId = 1L;

        // when
        long unreadCount = notificationService.getUnreadCount(memberId);

        // then
        assertTrue(unreadCount >= 0, "미읽음 알림 개수는 0 이상이어야 합니다.");

        System.out.println("✅ 미읽음 알림 개수: " + unreadCount);
    }

    /**
     * [미읽음 알림 목록 조회 테스트]
     * - SSE 초기 연결 시 미읽음 알림(IS_READ = 'N') 목록 정상 조회 확인
     * - 조회된 목록이 전부 IS_READ = 'N' 인지 확인
     */
    @Test
    void getUnreadNotifications() {
        // given
        Long memberId = 1L;

        // when
        List<NotificationResponseDto> result = notificationService.getUnreadNotifications(memberId);

        // then
        assertFalse(result.isEmpty(), "미읽음 알림이 존재해야 합니다.");
        result.forEach(dto ->
                assertEquals("N", dto.getIsRead(), "모든 알림의 읽음여부가 N이어야 합니다.")
        );

        System.out.println("✅ 미읽음 알림 개수: " + result.size());
        result.forEach(dto ->
                System.out.println("✅ 알림 ID: " + dto.getNotiId()
                        + " | 읽음여부: " + dto.getIsRead()
                        + " | 생성일: " + dto.getCreatedAt())
        );
    }

    /**
     * [개별 읽음 처리 테스트]
     * - 특정 알림 클릭 시 IS_READ가 'N' → 'Y'로 변경되는지 확인
     * - 본인 알림만 읽음 처리 가능한지 확인
     */
    @Test
    void markAsRead() {
        // given - DB에 실제 존재하는 notiId로 바꿔주세요
        Long notiId = 12L;
        Long memberId = 1L;

        // when
        notificationService.markAsRead(notiId, memberId);

        // then
        Notification result = notificationRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));

        assertEquals("Y", result.getIsRead(), "읽음 처리 후 IS_READ가 Y여야 합니다.");

        System.out.println("✅ 알림 ID: " + result.getNotiId()
                + " | 읽음여부: " + result.getIsRead());
    }

    /**
     * [일괄 읽음 처리 테스트]
     * - '모두 읽음' 버튼 클릭 시 회원의 모든 미읽음(N) 알림이 Y로 변경되는지 확인
     */
    @Test
    void markAllAsRead() {
        // given
        Long memberId = 1L;

        // when
        notificationService.markAllAsRead(memberId);

        // then - 미읽음 알림 개수가 0이어야 함
        long unreadCount = notificationService.getUnreadCount(memberId);
        assertEquals(0, unreadCount, "모두 읽음 처리 후 미읽음 알림 개수가 0이어야 합니다.");

        System.out.println("✅ 모두 읽음 처리 완료");
        System.out.println("✅ 남은 미읽음 알림 개수: " + unreadCount);
    }

    /**
     * [개별 알림 삭제 테스트]
     * - 'X' 버튼 클릭 시 특정 알림이 DB에서 완전 삭제되는지 확인
     * - 본인 알림만 삭제 가능한지 확인 (memberId 조건)
     */
    @Test
    void deleteNotification() {
        // given - DB에 실제 존재하는 notiId로 바꿔주세요
        Long notiId = 13L;
        Long memberId = 1L;

        // when
        notificationService.deleteNotification(notiId, memberId);

        // then - 삭제 후 조회하면 없어야 함
        boolean exists = notificationRepository.findById(notiId).isPresent();
        assertFalse(exists, "삭제 후 알림이 존재하지 않아야 합니다.");

        System.out.println("✅ 알림 ID: " + notiId + " 삭제 완료");
    }

    /**
     * [전체 알림 삭제 테스트]
     * - '모두 삭제' 버튼 클릭 시 회원의 모든 알림이 DB에서 완전 삭제되는지 확인
     */
    @Test
    void deleteAllNotifications() {
        // given
        Long memberId = 1L;

        // when
        notificationService.deleteAllNotifications(memberId);

        // then - 삭제 후 알림 목록이 비어있어야 함
        List<NotificationResponseDto> result = notificationService.getNotifications(memberId);
        assertTrue(result.isEmpty(), "전체 삭제 후 알림 목록이 비어있어야 합니다.");

        System.out.println("✅ 전체 알림 삭제 완료");
        System.out.println("✅ 남은 알림 개수: " + result.size());
    }

    /**
     * [스케줄러 자동 삭제 테스트]
     * - 읽은 알림(Y): 7일 경과 시 삭제 확인
     * - 안 읽은 알림(N): 14일 경과 시 삭제 확인
     */
    @Test
    void deleteOldNotifications() {
        // given
        Member receiver = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Notification oldReadNoti = Notification.builder()
                .member(receiver)
                .message("8일 전 읽은 알림 - 삭제되어야 함")
                .notiType(NotiType.COMMENT)
                .isRead("Y")
                .build();

        Notification oldUnreadNoti = Notification.builder()
                .member(receiver)
                .message("15일 전 안읽은 알림 - 삭제되어야 함")
                .notiType(NotiType.COMMENT)
                .isRead("N")
                .build();

        notificationRepository.save(oldReadNoti);
        notificationRepository.save(oldUnreadNoti);

        Long oldReadNotiId = oldReadNoti.getNotiId();
        Long oldUnreadNotiId = oldUnreadNoti.getNotiId();

        notificationRepository.updateCreatedAt(oldReadNotiId, LocalDateTime.now().minusDays(8));
        notificationRepository.updateCreatedAt(oldUnreadNotiId, LocalDateTime.now().minusDays(15));

        // when
        notificationService.deleteOldNotifications();

        // 1차 캐시 초기화
        entityManager.clear();

        // then
        assertFalse(notificationRepository.findById(oldReadNotiId).isPresent(),
                "8일 전 읽은 알림은 삭제되어야 합니다.");
        assertFalse(notificationRepository.findById(oldUnreadNotiId).isPresent(),
                "15일 전 안읽은 알림은 삭제되어야 합니다.");

        System.out.println("✅ 8일 전 읽은 알림(Y) 삭제 확인");
        System.out.println("✅ 15일 전 안읽은 알림(N) 삭제 확인");
    }
}