package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private boolean isEnabled;

    @Column(nullable = false)
    private boolean taskEnabled;

    @Column(nullable = false)
    private boolean announcementEnabled;

    @Column(nullable = false)
    private boolean settlementEnabled;

    /**
     * 알림 선호 시각
     */
    private LocalTime announcementTime; // null이면 기본 22:00
    private LocalTime taskTime; // null이면 기본 08:00
    private LocalTime settlementTime; // null이면 기본 22:00

    /**
     * 타입별 ON/OFF 조회
     */
    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case TASK -> taskEnabled;
            case ANNOUNCEMENT -> announcementEnabled;
            case SETTLEMENT -> settlementEnabled;
            case DELETE_REQUEST -> true;  // 긴급성 고려: 항상 발송
        };
    }

    /**
     * 기본값(모든 타입 ON)
     */
    public static NotificationSetting createDefault(Member member) {
        return NotificationSetting.builder()
                .member(member)
                .taskEnabled(true)
                .announcementEnabled(true)
                .settlementEnabled(true)
                .build();
    }
}