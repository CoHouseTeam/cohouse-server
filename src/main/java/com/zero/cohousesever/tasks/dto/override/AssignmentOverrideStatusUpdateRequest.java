package com.zero.cohousesever.tasks.dto.override;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 담당자 변경 요청 상태 변경 DTO
 */
@Getter
@Setter
public class AssignmentOverrideStatusUpdateRequest {
  private OverrideStatus status; // ACCEPTED, REJECTED, PENDING

  @JsonAlias("actorId")
  private Long groupMemberId;
}
