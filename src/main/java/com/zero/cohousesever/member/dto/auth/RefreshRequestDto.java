package com.zero.cohousesever.member.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshRequestDto {

    private String refreshToken;
}
