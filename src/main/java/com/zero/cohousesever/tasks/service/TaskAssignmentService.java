package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 실제 주간/일간 할일 배정 및 스케줄 생성, 조회 등 담당
 */
@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

  private final TaskAssignmentRepository taskAssignmentRepository;

  /**
   * 할일 배정 생성
   */
  public void assignTaskManuallyOrRandomly() {
    // 구현 예정
  }

  /**
   * 할일 배정 목록 조회
   */
  public void getAssignmentsByWeekAndMember() {
    // 구현 예정
  }

  /**
   * 할일 상태 변경 (완료, 연기 등)
   */
  public void updateAssignmentStatus() {
    // 구현 예정
  }
}
