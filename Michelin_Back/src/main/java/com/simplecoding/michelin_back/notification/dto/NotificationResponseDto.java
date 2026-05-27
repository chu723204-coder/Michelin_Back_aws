package com.simplecoding.michelin_back.notification.dto;

import com.simplecoding.michelin_back.notification.entity.Notification;
import com.simplecoding.michelin_back.notification.entity.NotiType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

    private Long notiId;
    private String senderName;  // 추가 - "OO님이" 표시용
    private Long relatedId;     // 추가
    private String title;
    private String message;
    private NotiType notiType;
    private String targetUrl;
    private String isRead;
    private LocalDateTime createdAt;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .notiId(notification.getNotiId())
                .senderName(notification.getSender() != null
                        ? notification.getSender().getName()
                        : null)  // 시스템 알림은 NULL
                .relatedId(notification.getRelatedId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notiType(notification.getNotiType())
                .targetUrl(notification.getTargetUrl())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}