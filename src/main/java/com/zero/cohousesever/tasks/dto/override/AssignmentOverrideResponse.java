package com.zero.cohousesever.tasks.dto.override;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

  public static AssignmentOverrideResponse from(AssignmentOverride e) {
    return AssignmentOverrideResponse.builder()
        .requestId(e.getId())
        .assignmentId(e.getAssignment().getId())
        .requesterId(e.getRequesterId())
        .targetId(e.getTargetId())
        .modifierId(e.getModifierId())
        .status(e.getStatus())
        .requestedAt(e.getRequestedAt() == null ? null : e.getRequestedAt().toString())
        .respondedAt(e.getRespondedAt() == null ? null : e.getRespondedAt().toString())
        .build();
  }

  public static List<AssignmentOverrideResponse> fromAll(Collection<AssignmentOverride> list) {
    return list.stream().map(AssignmentOverrideResponse::from).collect(Collectors.toList());
  }

}
