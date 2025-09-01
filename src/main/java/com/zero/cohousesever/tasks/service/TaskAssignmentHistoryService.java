package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskAssignmentHistory;
import com.zero.cohousesever.tasks.repository.TaskAssignmentHistoryRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskAssignmentHistoryService {

  private final TaskAssignmentHistoryRepository historyRepository;

  // 상태변경 시 upsert 기록
  public void recordStatusChange(TaskAssignment a) {
    TaskAssignmentHistory h = historyRepository
        .findByAssignmentIdAndDate(a.getId(), a.getDate())
        .orElseGet(() -> TaskAssignmentHistory.builder()
            .assignmentId(a.getId())
            .date(a.getDate())
            .build());

    h.setGroupMemberId(a.getGroupMemberId());
    h.setCategory(a.getTemplate().getCategory());
    h.setStatus(a.getStatus());

    historyRepository.save(h);
  }

  // 조회
  public List<TaskAssignmentResponse> getAssignmentHistories(Long assignmentId) {
    return historyRepository.findByAssignmentIdOrderByDateDescIdDesc(assignmentId).stream()
        .map(TaskAssignmentResponse::fromHistory)
        .toList();
  }
}
