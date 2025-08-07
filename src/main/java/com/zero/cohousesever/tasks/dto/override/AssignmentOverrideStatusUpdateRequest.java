package com.zero.cohousesever.tasks.dto.override;

import lombok.Getter;
import lombok.Setter;

/**
 * 담당자 변경 요청 상태 변경 DTO
 */
@Getter
@Setter
public class AssignmentOverrideStatusUpdateRequest {
  private String status; // "ACCEPTED" or "REJECTED"
}
