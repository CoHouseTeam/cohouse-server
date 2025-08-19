package com.zero.cohousesever.tasks.dto.assignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.enums.AssignmentStatus;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

  public static TaskAssignmentResponse from(TaskAssignment a) {
    return TaskAssignmentResponse.builder()
        .assignmentId(a.getId())
        .groupMemberId(a.getGroupMemberId())
        .templateId(a.getTemplate().getId())
        .date(a.getDate().toString())
        .status(a.getStatus())
        .createdAt(a.getCreatedAt() == null ? null : a.getCreatedAt().toString())
        .category(a.getTemplate().getCategory())
        .build();
  }

  public static List<TaskAssignmentResponse> fromAll(Collection<TaskAssignment> list) {
    return list.stream().map(TaskAssignmentResponse::from).collect(Collectors.toList());
  }

}
