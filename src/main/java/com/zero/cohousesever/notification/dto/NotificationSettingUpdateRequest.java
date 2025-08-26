package com.zero.cohousesever.notification.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알림 설정 변경 요청 DTO
 * - null이 아닌 필드만 반영(부분 갱신)
 * - 모든 필드가 null이면 검증 실패로 간주
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationSettingUpdateRequest {

    private Boolean taskEnabled;         // null이면 변경 없음
    private Boolean announcementEnabled; // null이면 변경 없음
    private Boolean settlementEnabled;   // null이면 변경 없음

    @AssertTrue(message = "최소 하나의 설정 값이 필요합니다.")
    public boolean hasAnyField() {
        return taskEnabled != null || announcementEnabled != null || settlementEnabled != null;
    }
}