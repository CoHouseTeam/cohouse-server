package com.zero.cohousesever.tasks.dto.assignment;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 할일 배정 생성 요청 DTO
 */
@Getter
@Setter
public class TaskAssignmentRequest {
  private Long groupId;
  private String date;
  private Long templateId;
  private List<Long> groupMemberId;
}
