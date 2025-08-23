package com.zero.cohousesever.group.dto.group;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInviteDto {

    private Long groupId;
    private String inviteCode;
}
