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


  // 랜덤 유지용 null이면 템플릿 randomEnabled를 따라감
  private Boolean randomEnabled;

  // 수동 지정용 값이 오면 무조건 이 멤버로 배정 (그룹장만 사용)
  private Long fixedAssigneeId;
}