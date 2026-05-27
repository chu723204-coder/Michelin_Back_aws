package com.simplecoding.michelin_back.popub.repository;

import com.simplecoding.michelin_back.popub.entity.PopupAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PopubAdRepository extends JpaRepository<PopupAd, Long> {

    /**
     * [메인페이지용 치트키 쿼리]
     * 현재 날짜/시간이 노출 기간(START_DATE ~ END_DATE) 사이에 있고,
     * 활성화 여부가 'Y'인 광고 중에서 가장 최근에 등록된 광고 딱 1건만 조회합니다.
     */
    @Query(value = "SELECT * FROM (" +
            "    SELECT * FROM POPUP_AD " +
            "    WHERE IS_ACTIVE = 'Y' " +
            "      AND SYSDATE >= START_DATE " +
            "      AND SYSDATE < END_DATE + 1 " +  // 💡 여기에 +1을 해서 종료일 다음날 0시 직전까지 포함시킵니다.
            "    ORDER BY REG_DATE DESC" +
            ") WHERE ROWNUM = 1", nativeQuery = true)
    Optional<PopupAd> findActivePopupAd();
}