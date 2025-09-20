package com.zero.cohousesever.notification.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DeviceToken
 * - 웹 FCM 토큰을 저장/관리하는 엔티티(플랫폼 구분 없음).
 * - 멤버별 복수 기기/브라우저를 지원하며, active 플래그로 사용 가능 여부를 관리한다.
 */
@Entity
@Table(
        name = "device_token",
        indexes = {
                @Index(name = "idx_device_token_member_active", columnList = "member_id, active"),
                @Index(name = "idx_device_token_token_unique", columnList = "token", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeviceToken extends BaseEntity {

    /** 토큰 소유자(연관 멤버) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 웹 FCM 토큰(유니크) */
    @Column(nullable = false, length = 512, unique = true)
    private String token;

    /** 활성 여부(로그아웃·브라우저 변경 시 비활성화) */
    @Column(nullable = false)
    private boolean active;

    /** 마지막 사용 시각(발송/검증 시 갱신) */
    private LocalDateTime lastUsedAt;

    /** 성공적으로 사용되었음을 기록(마지막 사용 시각 갱신) */
    public void markUsedNow() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /** 토큰 비활성화 처리 */
    public void deactivate() {
        this.active = false;
    }

    /** 토큰 활성화 처리 */
    public void activate() {
        this.active = true;
    }

    /** 소유자 재지정(동일 토큰을 다른 계정이 사용하게 된 경우) */
    public void reassignOwner(Member newOwner) {
        this.member = newOwner;
    }
}