package com.simplecoding.michelin_back.social.service;

import com.simplecoding.michelin_back.member.repository.MemberRepository;
import com.simplecoding.michelin_back.social.entity.RestaurantBookmark;
import com.simplecoding.michelin_back.social.entity.RestaurantLike;
import com.simplecoding.michelin_back.social.repository.RestaurantBookmarkRepository;
import com.simplecoding.michelin_back.social.repository.RestaurantLikeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialService {
    private final MemberRepository memberRepository; // 이 부분이 꼭 있어야 findById가 작동해요!
    private final RestaurantBookmarkRepository bookmarkRepository;
    private final RestaurantLikeRepository likeRepository;

    // 1. 좋아요 토글
    @Transactional
    public boolean toggleLike(Long memberId, Long restaurantId) {
        // 검증: 멤버가 진짜 있는지 확인 (ORA-02291 방지)
        memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다. ID: " + memberId));

        Optional<RestaurantLike> existingLike = likeRepository.findByMemberIdAndRestaurantId(memberId, restaurantId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            RestaurantLike newLike = RestaurantLike.builder()
                    .memberId(memberId)
                    .restaurantId(restaurantId)
                    .build();
            likeRepository.save(newLike);
            return true;
        }
    }

    // 2. 북마크 토글
    @Transactional
    public boolean toggleBookmark(Long memberId, Long restaurantId) {
        // 검증: 멤버가 진짜 있는지 확인 (ORA-02291 방지)
        memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다. ID: " + memberId));

        Optional<RestaurantBookmark> existingBookmark = bookmarkRepository.findByMemberIdAndRestaurantId(memberId, restaurantId);

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            return false;
        } else {
            RestaurantBookmark newBookmark = RestaurantBookmark.builder()
                    .memberId(memberId)
                    .restaurantId(restaurantId)
                    .folderName("기본 폴더")
                    .build();
            bookmarkRepository.save(newBookmark);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long memberId, Long restaurantId) {
        return likeRepository.findByMemberIdAndRestaurantId(memberId, restaurantId).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long memberId, Long restaurantId) {
        return bookmarkRepository.existsByMemberIdAndRestaurantId(memberId, restaurantId);
    }
}
