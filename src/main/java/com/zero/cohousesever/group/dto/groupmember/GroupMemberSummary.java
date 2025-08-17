package com.zero.cohousesever.group.dto.groupmember;

import com.zero.cohousesever.group.enums.GroupMemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupMemberSummary {

    private Long id;
    private Long groupId;
    private Long memberId;
    private Boolean isLeader;
    private String nickname;
    private GroupMemberStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leavedAt;
}
