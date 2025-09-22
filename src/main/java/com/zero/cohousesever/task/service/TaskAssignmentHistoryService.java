package com.zero.cohousesever.task.service;

import com.zero.cohousesever.task.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.repository.TaskAssignmentHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskAssignmentHistoryService {

  private final TaskAssignmentHistoryRepository historyRepository;

  // 상태변경 시 upsert 기록

  @Transactional
  public void recordStatusChange(TaskAssignment a) {
    historyRepository.upsertHistory(
        a.getId(),
        a.getGroupMemberId(),
        a.getTemplate().getCategory(),
        a.getStatus().name(),
        java.sql.Date.valueOf(a.getDate())
    );
  }

  // 조회
  public List<TaskAssignmentResponse> getAssignmentHistories(Long assignmentId) {
    return historyRepository.findByAssignmentIdOrderByDateDescIdDesc(assignmentId).stream()
        .map(TaskAssignmentResponse::fromHistory)
        .toList();
  }

  //  사용자별 전체 이행 히스토리
  public List<TaskAssignmentResponse> getMemberHistories(
      Long groupId, Long memberId, LocalDate from, LocalDate to
  ) {
    return historyRepository.searchByGroupMemberAndDateRange(groupId, memberId, from, to)
        .stream().map(TaskAssignmentResponse::fromHistory).toList();
  }

  @Transactional
  public void recordCreatedAssignments(List<TaskAssignment> assignments) {
    for (TaskAssignment a : assignments) {
      historyRepository.upsertHistory(
          a.getId(),
          a.getGroupMemberId(),
          a.getTemplate().getCategory(),
          a.getStatus().name(),
          java.sql.Date.valueOf(a.getDate())
      );
    }
  }

}
