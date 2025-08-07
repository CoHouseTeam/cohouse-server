package com.zero.cohousesever.tasks.dto.template;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/**
 * PUT /tasks/templates/{template_id} 엔드포인트 처리 시 카테고리만 변경위해 사용
 */
public class TaskTemplateUpdateRequest {
  private String category;
}

