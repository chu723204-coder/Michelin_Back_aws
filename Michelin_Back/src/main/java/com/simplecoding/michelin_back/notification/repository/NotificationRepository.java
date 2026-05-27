package com.simplecoding.michelin_back.notification.repository;

import com.simplecoding.michelin_back.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 1. 정렬 기준 Top 30 조회
     */
    List<Notification> findTop30ByMember_MemberIdOrderByIsReadAscCreatedAtDesc(Long memberId);

    /**
     * 2. 무한스크롤 페이징 (5개씩)
     */
    @Query("SELECT n FROM Notification n WHERE n.member.memberId = :memberId " +
            "ORDER BY n.isRead ASC, n.createdAt DESC")
    List<Notification> findNotificationsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 3. 읽지 않은 알림 개수 (배지용)
     */
    long countByMember_MemberIdAndIsRead(Long memberId, String isRead);

    /**
     * 4. 일괄 읽음 처리 벌크 UPDATE
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 'Y' " +
            "WHERE n.member.memberId = :memberId AND n.isRead = 'N'")
    void markAllAsRead(@Param("memberId") Long memberId);

    /**
     * 5. 전체 알림 하드 삭제 (사용자 '전체 삭제')
     */
    void deleteByMember_MemberId(Long memberId);

    /**
     * 6. 스케줄러 벌크 하드 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE " +
            "(n.isRead = 'Y' AND n.createdAt < :readCutoff) OR " +
            "(n.isRead = 'N' AND n.createdAt < :unreadCutoff)")
    void deleteOldNotifications(@Param("readCutoff") LocalDateTime readCutoff,
                                @Param("unreadCutoff") LocalDateTime unreadCutoff);

    /**
     * 개별 알림 하드 삭제 (사용자 'X' 버튼)
     * memberId 조건 필수 - 본인 알림만 삭제 가능하도록 보안 처리
     */
    void deleteByNotiIdAndMember_MemberId(Long notiId, Long memberId);

    /**
     * 개별 읽음 처리
     * memberId 조건 필수 - 본인 알림만 수정 가능하도록 보안 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 'Y' " +
            "WHERE n.notiId = :notiId AND n.member.memberId = :memberId")
    void markAsRead(@Param("notiId") Long notiId, @Param("memberId") Long memberId);

    /**
     * SSE 초기 연결 시 미읽음 알림 조회
     */
    List<Notification> findByMember_MemberIdAndIsRead(Long memberId, String isRead);

    // NotificationRepository에 추가
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.createdAt = :createdAt WHERE n.notiId = :notiId")
    void updateCreatedAt(@Param("notiId") Long notiId, @Param("createdAt") LocalDateTime createdAt);
}