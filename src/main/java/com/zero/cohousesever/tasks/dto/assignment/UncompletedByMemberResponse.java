package com.zero.cohousesever.tasks.dto.assignment;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UncompletedByMemberResponse {
  private Long groupMemberId;
  private int count;
  private List<TaskAssignmentResponse> assignments;
}