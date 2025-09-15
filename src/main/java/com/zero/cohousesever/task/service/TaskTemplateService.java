package com.zero.cohousesever.task.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.task.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.task.dto.template.TaskTemplateUpdateRequest;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import com.zero.cohousesever.task.entity.enums.OverrideStatus;
import com.zero.cohousesever.task.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 할일 템플릿 생성 및 조회, 삭제 등 템플릿 관련 비즈니스 로직을 처리
 */

@Service
@RequiredArgsConstructor
public class TaskTemplateService {

  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayService repeatDayService;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskAssignmentHistoryService taskAssignmentHistoryService;
  private final AssignmentOverrideRepository assignmentOverrideRepository;
  private final AssignmentOverrideHistoryService assignmentOverrideHistoryService;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final long SYSTEM_ACTOR = 0L;

  /**
   * 할일 템플릿 목록 조회
   */
  public List<TaskTemplate> getAllTemplates(Long groupId) {
    return taskTemplateRepository.findByGroupId(groupId).stream()
        .filter(TaskTemplate::isActive)
        .toList();
  }

  /**
   * 템플릿 생성
   */
  public TaskTemplate createTemplate(Long groupId, String category, List<String> repeatDays, Boolean randomEnabled) {
    TaskTemplate saved = taskTemplateRepository.save(
        TaskTemplate.builder()
            .groupId(groupId)
            .category(category)
            .randomEnabled(Boolean.TRUE.equals(randomEnabled))
            .active(true)
            .build()
    );

    if (repeatDays != null && !repeatDays.isEmpty()) {
      for (String day : repeatDays) {
        RepeatDayRequest rd = new RepeatDayRequest();
        rd.setDayOfWeek(day);
        repeatDayService.addRepeatDay(saved.getId(), rd);
      }
    }
    return saved;
  }

  /**
   * 템플릿 수정
   */
  public TaskTemplate updateTemplate(Long templateId, TaskTemplateUpdateRequest req) {
    TaskTemplate t = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    // 1) 카테고리 (옵션)
    if (req.getCategory() != null && !req.getCategory().isBlank()) {
      t.setCategory(req.getCategory().trim());
    }

    // 2) 랜덤 여부 (옵션)
    if (req.getRandomEnabled() != null) {
      t.setRandomEnabled(req.getRandomEnabled());
    }

    // 3) 반복요일 교체 (옵션)
    if (req.getRepeatDays() != null) {
      repeatDayService.replaceRepeatDays(templateId, req.getRepeatDays());
    }

    return taskTemplateRepository.save(t);
  }

  /**
   * 템플릿 삭제(비활성화)
   */
  @Transactional
  public void deleteTemplate(Long templateId) {
    TaskTemplate t = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    // 소프트 삭제
    t.setActive(false);
    taskTemplateRepository.save(t);

    // 오늘~1주 미완료 배정 SKIPPED 처리 + 히스토리
    LocalDate today = LocalDate.now(KST);
    LocalDate end = today.plusWeeks(1);
    List<TaskAssignment> upcoming =
        taskAssignmentRepository.findByTemplate_IdAndDateBetween(t.getId(), today, end);

    List<TaskAssignment> changed = new ArrayList<>();
    for (TaskAssignment a : upcoming) {
      if (a.getStatus() == AssignmentStatus.COMPLETED) continue;

      // 배정 상태 SKIPPED 처리
      if (a.getStatus() != AssignmentStatus.SKIPPED) {
        a.setStatus(AssignmentStatus.SKIPPED);
        changed.add(a);
      }

      // 관련된 '대기 중' 교체 요청 모두 REJECTED 처리 + 히스토리 남김
      var pendings = assignmentOverrideRepository
          .findAllByAssignment_IdAndStatus(a.getId(), OverrideStatus.REQUESTED);

      for (var r : pendings) {
        r.setStatus(OverrideStatus.REJECTED);
        r.setModifierId(SYSTEM_ACTOR);          // 누가 거절 처리했는지 표시(시스템/관리자)
        assignmentOverrideRepository.save(r);

        // 히스토리 기록
        assignmentOverrideHistoryService.record(r, r.getTargetId(), SYSTEM_ACTOR, 0L);
      }

    }

    if (!changed.isEmpty()) {
      taskAssignmentRepository.saveAll(changed);
      changed.forEach(taskAssignmentHistoryService::recordStatusChange);
    }
  }

  public TaskTemplate getById(Long id) {
    return taskTemplateRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
  }

  // groupId만 필요할 때
  public Long getGroupIdByTemplateId(Long id) {
    return taskTemplateRepository.findById(id)
        .map(TaskTemplate::getGroupId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
  }

}