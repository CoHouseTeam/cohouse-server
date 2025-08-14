package com.zero.cohousesever.notification.dto;

/**
 * 알림 응답 DTO
 */
public class NotificationResponseDto {
    private Long id;
    private Long memberId;
    private String type;
    private String title;
    private String content;
    private boolean read;
    private String readAt;
    private String createdAt;
}
