package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.repository.AssignmentOverrideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 할일 담당자 변경 요청 처리
 */
@Service
@RequiredArgsConstructor
public class AssignmentOverrideService {

  private final AssignmentOverrideRepository assignmentOverrideRepository;

  /**
   * 담당자 변경 요청 생성
   */
  public void createOverrideRequest() {
    // 구현 예정
  }

  /**
   * 담당자 변경 요청 수락/거절
   */
  public void respondToOverrideRequest() {
    // 구현 예정
  }

}
