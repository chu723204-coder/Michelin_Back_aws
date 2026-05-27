package com.simplecoding.michelin_back.notice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponseDto {
    private Long noticeId;
    private String title;
    private String content;
    private Long writerId;
    private String fixYn;
    private String deleteYn;  // ✅ 추가
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
    private String formattedDate;
    private boolean isNew;
}
