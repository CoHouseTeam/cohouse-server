package com.zero.cohousesever.group.dto.group;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInviteUrlDto {

    private String inviteUrl;
    private Integer expiresIn;
}
