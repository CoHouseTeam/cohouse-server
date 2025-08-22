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

  //이번 주부터 적용 여부. null이면 true로 처리
  private Boolean applyThisWeek;

  // 주마다 랜덤 배정 여부. true면 랜덤, false면 직전 담당자 그대로 유지
  private Boolean randomEnabled;
}
