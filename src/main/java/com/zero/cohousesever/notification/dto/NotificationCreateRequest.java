package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * /**
 * 알림 생성 요청 DTO
 * - 필수: type, content (title은 선택)
 */
@Getter
@NoArgsConstructor
public class NotificationCreateRequest {

    @NotNull
    private NotificationType type;

    private String title;

    @NotBlank
    private String content;

    public NotificationCreateRequest(NotificationType type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }
}