package com.zero.cohousesever.member.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
}
