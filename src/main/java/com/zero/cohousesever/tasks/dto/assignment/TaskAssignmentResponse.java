package com.zero.cohousesever.tasks.dto.assignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zero.cohousesever.tasks.entity.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 제외

public class TaskAssignmentResponse {
  private Long assignmentId;
  private Long groupMemberId;
  private Long templateId;
  private String date;
  private AssignmentStatus status;
  private String createdAt;

  // ====== history 전용 (옵셔널) ======
  private Long historyId;
  private String category;
  private String updatedAt;
}
