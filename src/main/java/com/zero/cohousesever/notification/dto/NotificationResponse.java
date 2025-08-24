package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 * - 목록/상세 조회 응답에 사용
 * - data: FE 라우팅/표시용 부가정보(JSON->Map)
 */
@Getter
@Builder
public class NotificationResponse {
    Long id;
    NotificationType type;
    String title;
    String content;

    Long settlementId;
    Long taskId;
    Long groupId;

    boolean read;
    LocalDateTime readAt;
    LocalDateTime createdAt;

    /**
     * 엔티티 -> DTO 변환
     */
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .settlementId(n.getSettlementId())
                .taskId(n.getTaskId())
                .groupId(n.getGroupId())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}