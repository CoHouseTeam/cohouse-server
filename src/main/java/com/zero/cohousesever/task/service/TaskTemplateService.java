package com.zero.cohousesever.task.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.task.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 할일 템플릿 생성 및 조회, 삭제 등 템플릿 관련 비즈니스 로직을 처리
 */

@Service
@RequiredArgsConstructor
public class TaskTemplateService {

  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayService repeatDayService;

  /**
   * 할일 템플릿 목록 조회
   */
  public List<TaskTemplate> getAllTemplates(Long groupId) {
    return taskTemplateRepository.findByGroupId(groupId);
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
   * 템플릿 카테고리 수정
   */
  public TaskTemplate updateTemplate(Long templateId, String category) {
    TaskTemplate template = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    template.setCategory(category);
    return taskTemplateRepository.save(template);
  }

  /**
   * 템플릿 삭제
   */
  public void deleteTemplate(Long templateId) {
    if (!taskTemplateRepository.existsById(templateId)) {
      throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
    }
    taskTemplateRepository.deleteById(templateId);
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