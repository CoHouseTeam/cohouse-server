package com.zero.cohousesever.tasks.dto.assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * 할일 상태 변경 요청 DTO
 */
@Getter
@Setter
public class TaskAssignmentStatusUpdateRequest {
  private String status; // COMPLETED, DEFERRED 등
}
