package com.simplecoding.michelin_back.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequestDto {
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    // fixYn은 사용자가 입력하지 않을 수도 있으므로, 서비스 로직에서 "N"으로 처리하는 것이 좋습니다.
    @Builder.Default
    private String fixYn = "N";

    private Long noticeId;
}