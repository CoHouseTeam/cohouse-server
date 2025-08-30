package com.zero.cohousesever.group.dto.leaverequest;

import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LeaveRequestSummary {

    private Long id;
    private Long groupId;
    private Long memberId;
    private String reason;
    private LeaveRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    public static LeaveRequestSummary fromEntity(GroupLeaveRequest entity) {
        return LeaveRequestSummary.builder()
                .id(entity.getId())
                .groupId(entity.getGroup().getId())
                .memberId(entity.getMember().getId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .respondedAt(entity.getRespondedAt())
                .build();
    }
}
