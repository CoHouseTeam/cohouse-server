package com.zero.cohousesever.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class FcmTokenRegisterRequest {

    @NotBlank(message = "FCM 토큰이 필요합니다.")
    private String token;
}