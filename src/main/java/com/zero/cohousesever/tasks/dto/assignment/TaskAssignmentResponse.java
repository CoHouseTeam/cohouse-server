package com.zero.cohousesever.tasks.dto.assignment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskAssignmentResponse {
  private Long assignmentId;
  private Long groupMemberId;
  private Long templateId;
  private String date; // "2025-08-05"
  private String status; // PENDING, COMPLETED 등
  private String repeatType; // WEEKLY, NONE 등
  private String createdAt; // "2025-08-05T12:00:00"
}
