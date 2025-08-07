package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.tasks.service.AssignmentOverrideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class AssignmentOverrideController {

   private final AssignmentOverrideService overrideService;

  /**
   * 담당자 변경 요청
   */
  @PostMapping("/assignments/{assignmentId}/override-request")
  public String requestOverride(
      @PathVariable Long assignmentId,
      @RequestBody String request
  ) {
    return "담당자 변경 요청 생성";
  }

  /**
   * 담당자 변경 요청 수락/거절
   */
  @PutMapping("/override-requests/{requestId}")
  public String handleOverrideResponse(
      @PathVariable Long requestId,
      @RequestBody String request
  ) {
    return "담당자 변경 요청 응답";
  }
}
