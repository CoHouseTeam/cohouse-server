package com.zero.cohousesever.notification.dto;

import com.zero.cohousesever.notification.entity.DeviceToken;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FcmTokenResponse {
    private Long id;
    private String token;
    private boolean active;
    private LocalDateTime lastUsedAt;

    public static FcmTokenResponse from(DeviceToken t) {
        return FcmTokenResponse.builder()
                .id(t.getId())
                .token(t.getToken())
                .active(t.isActive())
                .lastUsedAt(t.getLastUsedAt())
                .build();
    }
}