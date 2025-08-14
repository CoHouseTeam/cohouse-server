package com.zero.cohousesever.tasks.dto.template;

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
}
