package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class GroupMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(columnDefinition = "TINYINT")
    private Boolean isLeader;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private GroupMemberStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime leavedAt;
}
