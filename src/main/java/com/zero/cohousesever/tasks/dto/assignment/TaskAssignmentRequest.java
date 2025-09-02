package com.zero.cohousesever.tasks.dto.assignment;

import com.fasterxml.jackson.annotation.JsonAlias;
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

  private Boolean randomEnabled;
  private Long fixedAssigneeId;       // MANUAL 고정 배정자

  // 구버전 키 수용
  @JsonAlias("assigneeId")
  private Long _compatAssigneeId;

  @JsonAlias("candidateIds")
  private List<Long> _compatCandidateIds;

  @JsonAlias("assignType")
  private String _compatAssignType; // 서버에선 안 써도 됨

  // 게터에서 통합
  public Long getFixedAssigneeId() {
    return fixedAssigneeId != null ? fixedAssigneeId : _compatAssigneeId;
  }
  public List<Long> getGroupMemberId() {
    return groupMemberId != null ? groupMemberId : _compatCandidateIds;
  }
}