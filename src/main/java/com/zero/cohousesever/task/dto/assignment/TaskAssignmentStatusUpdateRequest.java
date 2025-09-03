package com.zero.cohousesever.task.dto.assignment;

import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 할일 상태 변경 요청 DTO
 */
@Getter
@Setter
public class TaskAssignmentStatusUpdateRequest {
  private AssignmentStatus status;
}
