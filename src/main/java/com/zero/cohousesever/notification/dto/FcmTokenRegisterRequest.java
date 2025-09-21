package com.zero.cohousesever.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FcmTokenRegisterRequest {

    @Size(max = 512, message = "FCM 토큰은 최대 512자입니다.")
    @NotBlank(message = "FCM 토큰이 필요합니다.")
    private String token;
}