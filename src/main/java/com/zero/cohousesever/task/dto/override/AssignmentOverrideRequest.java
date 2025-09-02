package com.zero.cohousesever.task.dto.override;

import com.fasterxml.jackson.annotation.JsonAlias;
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

  @JsonAlias({"receiverId"})
  private Long targetId;            // 단일 대상 or null인 경우 전체

  @JsonAlias({"receiverIds"})
  private List<Long> targetIds;     // 여러 명 대상일 때 사용

  private Long requesterId;
  private Long swapAssignmentId; // 서로 변경용
}