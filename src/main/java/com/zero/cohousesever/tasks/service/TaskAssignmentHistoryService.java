package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
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

  @Transactional(readOnly = true)
  public List<TaskAssignmentResponse> getAssignmentHistories(
      Long assignmentId, Long memberId, LocalDate fromDate, LocalDate toDate
  ) {
    // TODO:
    // - historyRepository로 이력 조회
    return Collections.emptyList();
  }
}
