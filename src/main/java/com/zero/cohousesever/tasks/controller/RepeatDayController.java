package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.tasks.service.RepeatDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/templates/{templateId}/repeat-days")
@RequiredArgsConstructor
public class RepeatDayController {

  private final RepeatDayService repeatDayService;

  /**
   * 반복 요일 목록 조회
   */
  @GetMapping
  public String getRepeatDays(@PathVariable Long templateId) {
    return "반복 요일 목록 조회";
  }

  /**
   * 반복 요일 추가
   */
  @PostMapping
  public String addRepeatDay(@PathVariable Long templateId, @RequestBody String request) {
    return "반복 요일 추가";
  }

  /**
   * 반복 요일 삭제
   */
  @DeleteMapping("/{repeatDayId}")
  public String deleteRepeatDay(@PathVariable Long templateId, @PathVariable Long repeatDayId) {
    return "반복 요일 삭제";
  }
}
