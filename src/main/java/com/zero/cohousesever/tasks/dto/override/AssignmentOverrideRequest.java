package com.zero.cohousesever.tasks.dto.override;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 담당자 변경 요청 생성 DTO
 */
@Getter
@Setter
public class AssignmentOverrideRequest {
  private Long assignmentId;        // 필수
  private Long targetId;            // 1명 대상일 때 사용 (선택)
  private List<Long> targetIds;     // 여러 명 대상일 때 사용 (선택, null/empty면 전체로 처리)
}
