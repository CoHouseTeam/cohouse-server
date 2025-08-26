package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.entity.NotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 설정 응답 DTO
 * - 회원의 알림 설정 전체를 한 번에 반환합니다.
 * - 각 알림 타입별 ON/OFF 상태를 노출합니다.
 */
@Getter
@AllArgsConstructor
public class NotificationSettingResponse {

    private boolean taskEnabled;         // 할일 알림
    private boolean announcementEnabled; // 공지 알림
    private boolean settlementEnabled;   // 정산 알림

    /**
     * 엔티티 → DTO 변환
     */
    public static NotificationSettingResponse from(NotificationSetting s) {
        return new NotificationSettingResponse(
                s.isTaskEnabled(),
                s.isAnnouncementEnabled(),
                s.isSettlementEnabled()
        );
    }
}