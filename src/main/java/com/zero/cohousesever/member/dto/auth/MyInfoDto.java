package com.zero.cohousesever.member.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyInfoDto {

    private String email;
    private String name;
}
