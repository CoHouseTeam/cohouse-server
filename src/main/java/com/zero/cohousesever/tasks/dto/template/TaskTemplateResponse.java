package com.zero.cohousesever.tasks.dto.template;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TaskTemplateResponse {
  private Long templateId;
  private Long groupId;
  private String category;
  private LocalDateTime createdAt;
}
