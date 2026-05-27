package com.simplecoding.michelin_back.notice.service;

import com.simplecoding.michelin_back.common.ApiResponse;
import com.simplecoding.michelin_back.common.MapStruct;
import com.simplecoding.michelin_back.notice.dto.NoticeRequestDto;
import com.simplecoding.michelin_back.notice.dto.NoticeResponseDto;
import com.simplecoding.michelin_back.notice.entity.Notice;
import com.simplecoding.michelin_back.notice.repository.NoticeMapper;
import com.simplecoding.michelin_back.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MapStruct mapStruct;
    private final NoticeMapper noticeMapper;

    /**
     * 공지사항 등록
     */
    @Transactional
    public NoticeResponseDto registerNotice(NoticeRequestDto requestDto, Long adminId) { // 💡 String을 Long으로 변경
        Notice noticeEntity = mapStruct.toEntity(requestDto);

        // 이제 parseLong 할 필요 없이 바로 넣으면 됩니다.
        noticeEntity.setWriterId(adminId);
        noticeEntity.setDeleteYn("N");

        Notice savedNotice = noticeRepository.save(noticeEntity);
        return mapStruct.toResponseDto(savedNotice);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public NoticeResponseDto updateNotice(Long noticeId, NoticeRequestDto requestDto) {
        Notice entity = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. ID: " + noticeId));

        entity.setTitle(requestDto.getTitle());
        entity.setContent(requestDto.getContent());
        entity.setFixYn(requestDto.getFixYn());

        return noticeMapper.toResponseDto(entity);
    }

    /**
     * 소프트 삭제 (N -> Y)
     */
    @Transactional
    public void deleteNotice(Long noticeId) {
        // 1. 존재 여부 확인
        if (!noticeRepository.existsById(noticeId)) {
            throw new IllegalArgumentException("해당 ID의 공지사항이 없습니다: " + noticeId);
        }

        // 2. 💡 직접 작성한 쿼리 메서드 호출 (이것이 핵심입니다)
        noticeRepository.updateDeleteStatus(noticeId, "Y", LocalDateTime.now());
    }

    /**
     * [추가] 공지사항 복구 (Y -> N)
     * 관리자 페이지에서 '삭제됨' 상태인 글을 다시 살릴 때 사용합니다.
     */
    @Transactional
    public void restoreNotice(Long noticeId) {
        Notice entity = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("복구할 공지사항이 없습니다: " + noticeId));

        entity.setDeleteYn("N");
    }

    /**
     * [고객용] 목록 조회 (N만 최신순)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<NoticeResponseDto>> getCustomerNoticeList(int page) {

        // 1. 페이징 설정
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page - 1, 10);

        // 2. 레포지토리 호출: InsertTime -> CreatedAt으로 메서드 이름 변경 반영
        org.springframework.data.domain.Page<Notice> entityPage =
                noticeRepository.findByDeleteYnOrderByFixYnDescCreatedAtDesc("N", pageable);

        // 3. Entity 리스트를 Dto 리스트로 변환
        List<NoticeResponseDto> dtoList = entityPage.getContent().stream()
                .map(mapStruct::toResponseDto)
                .collect(Collectors.toList());

        // 4. ApiResponse 반환
        return ApiResponse.success(
                dtoList,
                entityPage.getTotalPages(),
                (int) entityPage.getTotalElements()
        );
    }

    /**
     * [추가] [관리자용] 목록 조회 (N + Y 전체 최신순)
     */
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getAdminNoticeList() {
        // 리포지토리에 수정한 findAllByOrderByCreatedAtDesc() 사용
        List<Notice> entities = noticeRepository.findAllByOrderByCreatedAtDesc();
        return entities.stream()
                .map(mapStruct::toResponseDto)
                .collect(Collectors.toList());
    }
}