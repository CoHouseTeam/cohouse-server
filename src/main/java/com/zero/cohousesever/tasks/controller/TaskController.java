package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateRequest;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateResponse;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateUpdateRequest;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.service.AssignmentOverrideHistoryService;
import com.zero.cohousesever.tasks.service.AssignmentOverrideService;
import com.zero.cohousesever.tasks.service.RepeatDayService;
import com.zero.cohousesever.tasks.service.TaskAssignmentHistoryService;
import com.zero.cohousesever.tasks.service.TaskAssignmentService;
import com.zero.cohousesever.tasks.service.TaskTemplateService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskTemplateService taskTemplateService;
  private final RepeatDayService repeatDayService;
  private final TaskAssignmentService taskAssignmentService;
  private final AssignmentOverrideService overrideService;
  private final TaskAssignmentHistoryService taskAssignmentHistoryService;
  private final AssignmentOverrideHistoryService assignmentOverrideHistoryService;

  // 1. 템플릿 관련

  // 조회
  @GetMapping("/templates")
  public ResponseEntity<?> getTemplates(@RequestParam Long groupId) {
    List<TaskTemplateResponse> body = taskTemplateService.getAllTemplates(groupId).stream()
        .map(TaskTemplateResponse::from)
        .collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(body);
  }

  // 생성
  @PostMapping("/templates")
  public ResponseEntity<TaskTemplateResponse> createTemplate(
      @RequestBody TaskTemplateRequest request) {
    TaskTemplate saved = taskTemplateService.createTemplate(
        request.getGroupId(),
        request.getCategory(),
        request.getRepeatDays()
    );
    return ResponseEntity.ok(TaskTemplateResponse.from(saved));
  }

  // 수정
  @PutMapping("/templates/{templateId}")
  public ResponseEntity<TaskTemplateResponse> updateTemplate(@PathVariable Long templateId,
      @RequestBody TaskTemplateUpdateRequest request) {
    TaskTemplate updated = taskTemplateService.updateTemplate(templateId, request.getCategory());
    return ResponseEntity.ok(TaskTemplateResponse.from(updated));
  }

  // 삭제
  @DeleteMapping("/templates/{templateId}")
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
    taskTemplateService.deleteTemplate(templateId);
    return ResponseEntity.noContent().build();
  }

  // 2. 반복 요일 관련

  // 조회
  @GetMapping("/templates/{templateId}/repeat-days")
  public ResponseEntity<List<RepeatDayResponse>> getRepeatDays(@PathVariable Long templateId) {
    return ResponseEntity.ok(repeatDayService.getRepeatDaysByTemplateId(templateId));
  }

  // 생성
  @PostMapping("/templates/{templateId}/repeat-days")
  public ResponseEntity<RepeatDayResponse> addRepeatDay(
      @PathVariable Long templateId,
      @RequestBody RepeatDayRequest request) {
    RepeatDayResponse created = repeatDayService.addRepeatDay(templateId, request);
    return ResponseEntity.ok(created);
  }

  // 삭제
  @DeleteMapping("/templates/{templateId}/repeat-days/{repeatDayId}")
  public ResponseEntity<Void> deleteRepeatDay(
      @PathVariable Long templateId,
      @PathVariable Long repeatDayId
  ) {
    repeatDayService.deleteRepeatDay(templateId, repeatDayId);
    return ResponseEntity.noContent().build();
  }

  // 3. 할일 배정 관련
  @GetMapping("/assignments")
  public ResponseEntity<List<TaskAssignmentResponse>> getAssignments() {
    return null;
  }

  @PostMapping("/assignments")
  public ResponseEntity<TaskAssignmentResponse> assignTask(
      @RequestBody TaskAssignmentRequest request) {
    return null;
  }

  @PutMapping("/assignments/{assignmentId}")
  public ResponseEntity<TaskAssignmentResponse> updateAssignmentStatus(
      @PathVariable Long assignmentId, @RequestBody TaskAssignmentRequest request) {
    return null;
  }

  // 4. 담당자 변경 요청 관련
  @PostMapping("/assignments/{assignmentId}/override-request")
  public ResponseEntity<AssignmentOverrideRequest> requestOverride() {
    return null;
  }

  @PutMapping("/override-requests/{requestId}")
  public ResponseEntity<AssignmentOverrideResponse> respondOverride() {
    return null;
  }

  // 할일 이행 히스토리 조회
  @GetMapping("/assignments/{assignmentId}/histories")
  public ResponseEntity<List<TaskAssignmentResponse>> getTaskAssignmentHistories(
      @PathVariable Long assignmentId
  ) {
    return ResponseEntity.ok(Collections.emptyList()); // TODO
  }

  // 담당자 변경 요청 히스토리 조회
  @GetMapping("/override-requests/{requestId}/histories")
  public ResponseEntity<List<AssignmentOverrideResponse>> getAssignmentOverrideHistories(

  ) {
    return ResponseEntity.ok(Collections.emptyList()); // TODO
  }
}


