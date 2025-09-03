package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * /**
 * 알림 생성 요청 DTO
 * - 필수: type, content (title은 선택)
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateRequest {

    @NotNull
    private NotificationType type;

    private String title;

    @NotBlank
    private String content;
}