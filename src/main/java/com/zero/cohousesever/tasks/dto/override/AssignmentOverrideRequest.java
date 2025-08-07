package com.zero.cohousesever.tasks.dto.override;

import lombok.Getter;
import lombok.Setter;

/**
 * 담당자 변경 요청 생성 DTO
 */
@Getter
@Setter
public class AssignmentOverrideRequest {
  private Long requesterId; // 요청자
  private Long receiverId;  // 요청받는 사용자
}
