package com.zero.cohousesever.tasks.dto.template;

import com.zero.cohousesever.tasks.entity.TaskTemplate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskTemplateResponse {
  private Long templateId;
  private Long groupId;
  private String category;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean randomEnabled;

  // api 구조 반환형태
  public static TaskTemplateResponse from(TaskTemplate t) {
    return TaskTemplateResponse.builder()
        .templateId(t.getId())
        .groupId(t.getGroupId())
        .category(t.getCategory())
        .createdAt(t.getCreatedAt())
        .updatedAt(t.getUpdatedAt())
        .build();
  }
}
