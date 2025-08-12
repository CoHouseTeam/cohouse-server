package com.zero.cohousesever.member.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordForgotRequestDto {

    private String email;
    private String name;
}
