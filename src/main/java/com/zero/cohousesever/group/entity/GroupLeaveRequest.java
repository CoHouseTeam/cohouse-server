package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_leave_requests")
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

    public void respond(LeaveRequestStatus status) {
        this.status = status;
        this.respondedAt = LocalDateTime.now();
    }
}
