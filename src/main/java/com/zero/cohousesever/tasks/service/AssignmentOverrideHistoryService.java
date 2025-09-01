package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import com.zero.cohousesever.tasks.entity.AssignmentOverrideHistory;
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

  // 응답(수락/거절) 기록
  public void record(AssignmentOverride r, Long targetId, Long actorMemberId, Long postId) {
    AssignmentOverrideHistory h = AssignmentOverrideHistory.builder()
        .request(r)
        .targetId(targetId)
        .modifierId(actorMemberId)
        .postId(postId == null ? 0L : postId) // 게시판 미연동 시 0L
        .status(r.getStatus())
        .requestedAt(r.getRequestedAt())
        .respondedAt(r.getRespondedAt() != null ? r.getRespondedAt() : java.time.LocalDateTime.now())
        .build();

    historyRepository.save(h);
  }

  public List<AssignmentOverrideResponse> getOverrideHistories(Long requestId) {
    return historyRepository.findByRequest_IdOrderByRespondedAtDescIdDesc(requestId).stream()
        .map(AssignmentOverrideResponse::fromHistory)
        .toList();
  }
}
