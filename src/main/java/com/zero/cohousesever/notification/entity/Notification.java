package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member memberId;

    private String type;
    private String title;
    private String content;

    private boolean isRead;
    private LocalDateTime readAt;
}
