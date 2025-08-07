package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
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

  /**
   * 할일 템플릿 목록 조회
   */
  public List<TaskTemplate> getAllTemplates(Long groupId) {
    // TODO: 구현
    return null;
  }

  /**
   * 템플릿 생성
   */
  public TaskTemplate createTemplate(Long groupId, String category) {
    // TODO: 구현
    return null;
  }

  /**
   * 템플릿 수정
   */
  public TaskTemplate updateTemplate(Long templateId, String category) {
    return null;
  }

  /**
   * 템플릿 삭제
   */
  public void deleteTemplate(Long templateId) {

  }
}

