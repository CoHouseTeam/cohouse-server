package com.zero.cohousesever.tasks.dto.override;

import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentOverrideResponse {
  private Long requestId;
  private Long assignmentId;
  private Long requesterId;
  private Long targetId;
  private Long modifierId;
  private OverrideStatus status;         // ACCEPTED, REJECTED, PENDING
  private String requestedAt;    // 요청 시간
  private String respondedAt;    // 응답 시간 (nullable)
}
