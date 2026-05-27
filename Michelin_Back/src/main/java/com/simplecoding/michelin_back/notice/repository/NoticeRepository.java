package com.simplecoding.michelin_back.notice.repository;

import com.simplecoding.michelin_back.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // 추가
import org.springframework.data.jpa.repository.Query;    // 추가
import org.springframework.data.repository.query.Param; // 추가
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByDeleteYnOrderByFixYnDescCreatedAtDesc(String deleteYn, Pageable pageable);

    List<Notice> findAllByOrderByCreatedAtDesc();

    Optional<Notice> findByNoticeIdAndDeleteYn(Long noticeId, String deleteYn);

    // 💡 소프트 삭제를 위한 명시적 업데이트 쿼리 추가
    @Modifying
    @Query("UPDATE Notice n SET n.deleteYn = :deleteYn, n.deleteTime = :deleteTime WHERE n.noticeId = :noticeId")
    void updateDeleteStatus(@Param("noticeId") Long noticeId,
                            @Param("deleteYn") String deleteYn,
                            @Param("deleteTime") LocalDateTime deleteTime);
}