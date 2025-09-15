package com.zero.cohousesever.task.dto.template;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/**
 * PUT /tasks/templates/{template_id} 카테고리, 랜덤여부, 반복요일 수정 가능
 */
public class TaskTemplateUpdateRequest {
  private String category;
  private Boolean randomEnabled;
  private List<String> repeatDays;
}

