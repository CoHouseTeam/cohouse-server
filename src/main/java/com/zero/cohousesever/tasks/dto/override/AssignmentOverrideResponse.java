package com.zero.cohousesever.tasks.dto.override;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentOverrideResponse {
  private Long requestId;
  private Long assignmentId;
  private Long requesterId;
  private Long receiverId;
  private String status;         // REQUESTED, ACCEPTED, REJECTED
  private String requestedAt;    // 요청 시간
  private String respondedAt;    // 응답 시간 (nullable)
}
