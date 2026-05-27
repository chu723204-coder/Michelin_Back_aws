package com.simplecoding.michelin_back.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotiType {
    LIKE("좋아요"),
    BOOKMARK("북마크"),
    COMMENT("댓글"),    // 원댓글 알림
    REPLY("답글"),      // 답글 알림
    SYSTEM("시스템");

    private final String description;
}