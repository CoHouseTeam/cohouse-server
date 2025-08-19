package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
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
// private final GroupService groupService; 그룹장 확인 여부를 위해 그룹 서비스 머지후 사용


  /**
   * 할일 템플릿 목록 조회
   */
  public List<TaskTemplate> getAllTemplates(Long groupId) {
    return taskTemplateRepository.findByGroupId(groupId);
  }

  /**
   * 템플릿 생성
   */
  public TaskTemplate createTemplate(Long groupId, String category, List<String> repeatDays) {
    TaskTemplate saved = taskTemplateRepository.save(
        TaskTemplate.builder()
            .groupId(groupId)
            .category(category)
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
    TaskTemplate template =
        taskTemplateRepository.findById(templateId)
            .orElseThrow(
                () -> new EntityNotFoundException("TaskTemplate not found: " + templateId));
    template.setCategory(category);
    return taskTemplateRepository.save(template);
  }



  /**
   * 템플릿 삭제
   */
  public void deleteTemplate(Long templateId) {
    if (!taskTemplateRepository.existsById(templateId)) {
      throw new EntityNotFoundException("TaskTemplate not found: " + templateId);
    }
    taskTemplateRepository.deleteById(templateId);
  }

}