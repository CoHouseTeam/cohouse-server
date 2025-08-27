package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.converter.JsonMapConverter;
import com.zero.cohousesever.notification.type.NotificationStatus;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 엔티티
 * - Member 1:N Notification 관계(다대일 연관)
 * - data: 부가 정보(JSON) 저장용(예: settlementId, taskId, deeplink 등)
 * - 보관 정책: 최근 30일 유지, 이후 소프트 딜리트
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private Long settlementId;
    private Long taskId;
    private Long groupId;

    @Column(nullable = false)
    private boolean isRead;

    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private LocalDateTime deletedAt;
}
