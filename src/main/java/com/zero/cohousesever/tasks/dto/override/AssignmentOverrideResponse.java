package com.zero.cohousesever.tasks.dto.override;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentOverrideResponse {
  private Long requestId;
  private Long assignmentId;
  private Long requesterId;
  private Long targetId;
  private List<Long> targetIds;
  private Long modifierId;
  private OverrideStatus status; // ACCEPTED, REJECTED, PENDING
  private String requestedAt;    // 요청 시간
  private String respondedAt;    // 응답 시간 (nullable)

  // ====== history 전용 (옵셔널) ======
  private Long historyId;
  private Long postId;
}
