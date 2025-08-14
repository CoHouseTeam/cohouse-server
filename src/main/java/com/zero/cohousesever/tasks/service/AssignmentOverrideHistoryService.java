package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.repository.AssignmentOverrideHistoryRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentOverrideHistoryService {

  private final AssignmentOverrideHistoryRepository historyRepository;

  @Transactional(readOnly = true)
  public List<AssignmentOverrideResponse> getOverrideHistories(
      Long memberId, LocalDate fromDate, LocalDate toDate
  ) {
    // TODO:
    // - historyRepository로 이력 조회 (memberId)
    return Collections.emptyList();
  }
}
