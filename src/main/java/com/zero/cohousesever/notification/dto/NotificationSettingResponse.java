package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 설정 응답 DTO
 * - type과 enabled만 노출합니다.
 */
@Getter
@AllArgsConstructor
public class NotificationSettingResponse {
    private NotificationType type;
    private boolean isEnabled;

    public static NotificationSettingResponse from(NotificationSetting s) {
        return new NotificationSettingResponse(s.getType(), s.isEnabled());
    }
}