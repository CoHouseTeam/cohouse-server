package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.tasks.service.TaskAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/assignments")
@RequiredArgsConstructor
public class TaskAssignmentController {

  private final TaskAssignmentService taskAssignmentService;

  /**
   * 할일 배정 목록 조회
   */
  @GetMapping
  public String getAssignments(
      @RequestParam Long groupId,
      @RequestParam String week,
      @RequestParam(required = false) Long memberId
  ) {
    return "할일 배정 목록 조회";
  }

  /**
   * 할일 배정 생성 (랜덤/수동 배정)
   */
  @PostMapping
  public String createAssignment(@RequestBody String request) {
    return "할일 배정 생성";
  }

  /**
   * 할일 상태 변경 (예: 완료, 연기 등)
   */
  @PutMapping("/{assignmentId}")
  public String updateAssignmentStatus(@PathVariable Long assignmentId, @RequestBody String request) {
    return "할일 상태 변경";
  }
}
