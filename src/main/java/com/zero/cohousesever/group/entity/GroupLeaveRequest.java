package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class GroupLeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveRequestStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime respondedAt;
}
