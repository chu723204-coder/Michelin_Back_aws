package com.simplecoding.michelin_back.review.service;

import com.simplecoding.michelin_back.review.dto.ReviewRequestDto;
import com.simplecoding.michelin_back.review.entity.RestaurantReview;
import com.simplecoding.michelin_back.review.entity.ReviewReaction;
import com.simplecoding.michelin_back.review.repository.RestaurantReviewRepository;
import com.simplecoding.michelin_back.review.repository.ReviewReactionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@Transactional // 테스트가 끝나면 DB 데이터를 자동으로 롤백해줍니다. (실제 반영을 원하면 주석 처리)
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RestaurantReviewRepository reviewRepository;

    @Autowired
    private RestaurantReviewRepository restaurantReviewRepository; // 정확한 명칭으로 주입

    @Autowired
    private ReviewReactionRepository reviewReactionRepository; // 정확한 명칭으로 주입

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("리뷰 작성 및 DB 저장 테스트")
    void createReviewTest() {
        // given
        ReviewRequestDto dto = ReviewRequestDto.builder()
                .restaurantId(1139L)
                .memberId(10L)
                .content("DB 테스트 리뷰입니다.")
                .rating(5)
                .build();

        // when
        Long reviewId = reviewService.createReview(dto);

        // then
        Optional<RestaurantReview> savedReview = reviewRepository.findById(reviewId);
        assertThat(savedReview).isPresent();
        assertThat(savedReview.get().getContent()).isEqualTo("DB 테스트 리뷰입니다.");
    }

    @Test
    @DisplayName("리뷰 삭제 시 DB 변경 및 SSE 알림 발송 확인")
    void deleteReviewWithSseTest() {
        // 1. Given: 리뷰 저장
        RestaurantReview review = RestaurantReview.builder()
                .restaurantId(1139L)
                .memberId(10L)
                .content("SSE 알림 테스트용 리뷰")
                .rating(3)
                .isDeleted("N")
                .build();
        RestaurantReview saved = reviewRepository.save(review);
        Long targetId = saved.getReviewId();

        // 2. When: 삭제 서비스 호출 (이 안에서 sseEmitters.broadcast()가 실행됨)
        System.out.println(">>> 삭제 서비스 호출 시작");
        reviewService.deleteReview(targetId);
        System.out.println(">>> 삭제 서비스 호출 종료");

        // 3. Then: DB 상태 검증
        RestaurantReview deletedReview = reviewRepository.findById(targetId)
                .orElseThrow(() -> new AssertionError("리뷰를 찾을 수 없습니다."));

        assertThat(deletedReview.getIsDeleted()).isEqualTo("Y");

        System.out.println("삭제 여부(IS_DELETED): " + deletedReview.getIsDeleted());
    }

    @Test
    @DisplayName("좋아요 토글 시 DB에 데이터가 생성/삭제되는지 테스트")
    void toggleReactionTest() {
        // given
        RestaurantReview review = RestaurantReview.builder()
                .restaurantId(1139L).memberId(10L).content("좋아요 테스트").rating(5).build();
        RestaurantReview saved = reviewRepository.save(review);

        // when & then 1: 좋아요 누름 (데이터 생성)
        reviewService.toggleReaction(saved.getReviewId(), 100L, "LIKE");
        // 실제 DB에 반응 데이터가 있는지 확인하는 로직 추가 가능

//        // when & then 2: 한 번 더 누름 (데이터 삭제/취소)
//        reviewService.toggleReaction(saved.getReviewId(), 100L, "LIKE");
    }

    @Test
    @DisplayName("DB에 등록된 실제 리뷰 데이터로 평균 별점 검증")
    void getAverageRatingRealDataTest() {
        // 1. 준비: 스크린샷에서 확인한 식당 ID
        Long restaurantId = 1139L;

        // 2. 실행: 서비스(또는 리포지토리 쿼리)에서 평균 가져오기
        Double serviceAvg = restaurantReviewRepository.getAverageRating(restaurantId);

        // 만약 리뷰가 하나도 없다면 null이 반환될 수 있으므로 처리
        if (serviceAvg == null) serviceAvg = 0.0;

        // 서비스 로직에서 소수점 처리를 한다면 동일하게 적용
        serviceAvg = Math.round(serviceAvg * 10) / 10.0;

        // 3. 검증: 직접 계산을 위해 '전체 리뷰'를 가져옴 (식당 ID 기준)
        // 리포지토리에 해당 메서드가 없으므로 findAll() 후 필터링하거나
        // 혹은 간단하게 쿼리로 다시 한번 검증합니다.
        List<RestaurantReview> allReviews = restaurantReviewRepository.findAll();

        double sum = 0;
        int count = 0;

        for (RestaurantReview r : allReviews) {
            // 해당 식당의 리뷰이면서 삭제되지 않은('N') 것들만 직접 합산
            if (r.getRestaurantId().equals(restaurantId) && "N".equals(r.getIsDeleted())) {
                sum += r.getRating();
                count++;
            }
        }

        // 4. 직접 계산한 평균값
        double manualAvg = (count == 0) ? 0.0 : sum / count;
        manualAvg = Math.round(manualAvg * 10) / 10.0;

        System.out.println("조회된 식당 ID: " + restaurantId);
        System.out.println("계산에 포함된 리뷰 개수: " + count);
        System.out.println("직접 계산 평균: " + manualAvg);
        System.out.println("쿼리 결과 평균: " + serviceAvg);

        // 5. 최종 비교
        assertThat(serviceAvg).isEqualTo(manualAvg);
    }

    @Test
    @DisplayName("기존 LIKE 반응이 DISLIKE로 정상 업데이트되는지 테스트")
    @Rollback(false)
    void switchLikeToDislikeTest() {
        // 1. 준비: SQL Developer에서 확인한 데이터 기준
        Long reviewId = 7L;
        Long memberId = 100L;

        // 2. 실행: '싫어요' 버튼 클릭 시뮬레이션
        reviewService.toggleReaction(reviewId, memberId, "DISLIKE");

        // 3. 검증: 정확한 Repository 명칭을 사용하여 조회
        RestaurantReview review = restaurantReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AssertionError("리뷰가 존재해야 합니다."));

        ReviewReaction updatedReaction = reviewReactionRepository.findByMemberIdAndReview(memberId, review)
                .orElseThrow(() -> new AssertionError("반응 데이터가 존재해야 합니다."));

        // 타입이 DISLIKE로 바뀌었는지 최종 확인
        assertThat(updatedReaction.getReactionType()).isEqualTo("DISLIKE");

        System.out.println("변경 후 Reaction ID: " + updatedReaction.getReactionId());
        System.out.println("변경 후 타입: " + updatedReaction.getReactionType());
    }

    @Test
    @DisplayName("기존 9번 리뷰에 답글 등록 테스트")
    @Transactional
    @Rollback(false)
    void createReplyToReviewNo9() {
        // 1. Given: 이미 DB에 존재하는 9번 리뷰를 부모로 설정
        Long parentId = 9L;

        // 부모 리뷰가 실제로 존재하는지 먼저 확인 (없으면 테스트 실패)
        RestaurantReview parent = reviewRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("9번 리뷰가 DB에 없습니다. ID를 다시 확인해주세요."));

        ReviewRequestDto replyDto = new ReviewRequestDto();
        replyDto.setRestaurantId(1139L); // 원글과 동일한 식당
        replyDto.setMemberId(99L);       // 답글 작성자 ID (임의 설정)
        replyDto.setContent("9번 리뷰에 대한 답글입니다. 정말 공감되네요!");
        replyDto.setRating(0);           // 답글은 별점 0점 처리
        replyDto.setParentReviewId(parentId); // 부모 ID를 9로 설정

        // 2. When: 서비스 호출
        Long replyId = reviewService.createReview(replyDto);

        // DB 반영을 위해 강제 flush (에러 발생 여부 확인용)
        em.flush();
        em.clear();

        // 3. Then: 저장된 답글 조회 및 검증
        RestaurantReview savedReply = reviewRepository.findById(replyId).get();

        System.out.println("======================================");
        System.out.println("새로 생성된 답글 ID: " + replyId);
        System.out.println("연결된 부모 리뷰 ID: " + savedReply.getParent().getReviewId());
        System.out.println("답글 내용: " + savedReply.getContent());
        System.out.println("======================================");

        // 검증: 부모 ID가 9번인지 확인
        assertThat(savedReply.getParent().getReviewId()).isEqualTo(parentId);
        // 검증: 삭제 여부가 기본값 'N'으로 잘 들어갔는지 확인
        assertThat(savedReply.getIsDeleted()).isEqualTo("N");
    }


}