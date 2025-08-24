package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.type.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 변경 요청 DTO
 * - 특정 타입의 on/off 값을 전달합니다.
 */
@Getter
@NoArgsConstructor
public class NotificationSettingUpdateRequest {
    private NotificationType type;
    private boolean enabled;
}