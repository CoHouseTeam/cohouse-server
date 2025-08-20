package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.type.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 응답 DTO
 * - 목록/상세 조회 응답에 사용
 * - data: FE 라우팅/표시용 부가정보(JSON->Map)
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        Map<String, Object> data,// ex) {"deeplink":"/settlements/77","groupId":10}
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    /** 엔티티 -> DTO 변환 */
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getContent(),
                n.getData(),
                n.isRead(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}