package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "notification_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "type"})
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private boolean isEnabled;
}
