package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.*;

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
    private boolean taskEnabled;

    @Column(nullable = false)
    private boolean announcementEnabled;

    @Column(nullable = false)
    private boolean settlementEnabled;

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