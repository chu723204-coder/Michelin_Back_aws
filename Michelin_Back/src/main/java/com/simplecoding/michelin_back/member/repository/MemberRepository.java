package com.simplecoding.michelin_back.member.repository;

import com.simplecoding.michelin_back.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);

    /** 정지 만료된 회원 목록 (스케줄러용) */
    @Query("SELECT m FROM Member m WHERE m.status = 'SUSPENDED' AND m.suspendedUntil <= :today")
    List<Member> findExpiredSuspensions(@Param("today") LocalDate today);
}
