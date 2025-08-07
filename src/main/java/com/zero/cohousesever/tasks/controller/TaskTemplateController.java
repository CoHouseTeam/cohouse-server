package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.tasks.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/templates")
@RequiredArgsConstructor
public class TaskTemplateController {

   private final TaskTemplateService taskTemplateService;

  /**
   * 할일 템플릿 목록 조회
   */
  @GetMapping
  public String getAllTemplates() {
    return "할일 템플릿 목록 조회";
  }

  /**
   * 할일 템플릿 생성
   */
  @PostMapping
  public String createTemplate(@RequestBody String request) {
    return "할일 템플릿 생성";
  }

  /**
   * 할일 템플릿 수정
   */
  @PutMapping("/{templateId}")
  public String updateTemplate(@PathVariable Long templateId, @RequestBody String request) {
    return "할일 템플릿 수정";
  }

  /**
   * 할일 템플릿 삭제
   */
  @DeleteMapping("/{templateId}")
  public String deleteTemplate(@PathVariable Long templateId) {
    return "할일 템플릿 삭제";
  }
}
