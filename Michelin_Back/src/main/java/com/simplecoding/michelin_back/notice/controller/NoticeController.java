package com.simplecoding.michelin_back.notice.controller;

import com.simplecoding.michelin_back.common.ApiResponse;
import com.simplecoding.michelin_back.common.SecurityUtil;
import com.simplecoding.michelin_back.notice.dto.NoticeRequestDto;
import com.simplecoding.michelin_back.notice.dto.NoticeResponseDto;
import com.simplecoding.michelin_back.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 고객용 공지사항 목록 조회
    @GetMapping
    public ApiResponse<List<NoticeResponseDto>> getCustomerNoticeList(
            @RequestParam(defaultValue = "1") int page) {
        return noticeService.getCustomerNoticeList(page);
    }

    // 관리자용 공지사항 전체 목록 조회
    @GetMapping("/admin")
    public ApiResponse<List<NoticeResponseDto>> getAdminNoticeList() {
        List<NoticeResponseDto> list = noticeService.getAdminNoticeList();
        return ApiResponse.success(list);  // ✅
    }

    // 공지사항 등록
    @PostMapping("/admin")
    public ApiResponse<NoticeResponseDto> registerNotice(@RequestBody NoticeRequestDto requestDto) {
        // 💡 하드코딩된 숫자 1L을 사용합니다.
        // 실제 관리자 테이블에 ID가 1인 관리자가 있어야 합니다.
        Long adminId = 1L;

        NoticeResponseDto result = noticeService.registerNotice(requestDto, adminId);
        return ApiResponse.success(result);
    }

    // 공지사항 수정
    @PutMapping("/admin/{noticeId}")
    public ApiResponse<NoticeResponseDto> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeRequestDto requestDto) {
        NoticeResponseDto result = noticeService.updateNotice(noticeId, requestDto);
        return ApiResponse.success(result);  // ✅
    }

    // 공지사항 삭제
    @DeleteMapping("/admin/{noticeId}")
    public ApiResponse<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ApiResponse.success(noticeId + "번 공지사항이 삭제되었습니다.");  // ✅
    }

    // 공지사항 복구
    @PatchMapping("/admin/restore/{noticeId}")
    public ApiResponse<Void> restoreNotice(@PathVariable Long noticeId) {
        noticeService.restoreNotice(noticeId);
        return ApiResponse.success(noticeId + "번 공지사항이 복구되었습니다.");  // ✅
    }
}