package com.zero.cohousesever.group.dto.groupmember;

import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupMemberSummary {

    private static final String DEFAULT_PROFILE_URL = "https://cohouse-bucket.s3.ap-northeast-2.amazonaws.com/members/default/PersonCircle.png";

    private Long id;
    private Long groupId;
    private Long memberId;
    private Boolean isLeader;
    private String nickname;
    private GroupMemberStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leavedAt;

    private String profileImageUrl; // 프론트엔드 요청

    public static GroupMemberSummary fromEntity(GroupMember groupMember) {
        Member member = groupMember.getMember();
        String profileImageUrl = member.getProfileImageUrl();

        return GroupMemberSummary.builder()
                .id(groupMember.getId())
                .groupId(groupMember.getGroup().getId())
                .memberId(member.getId())
                .isLeader(groupMember.getIsLeader())
                .nickname(groupMember.getNickname())
                .status(groupMember.getStatus())
                .joinedAt(groupMember.getJoinedAt())
                .leavedAt(groupMember.getLeavedAt())
                .profileImageUrl(profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_URL)
                .build();
    }
}
