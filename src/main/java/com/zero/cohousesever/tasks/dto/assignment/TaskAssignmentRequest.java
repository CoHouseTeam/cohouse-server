package com.zero.cohousesever.tasks.dto.assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * 할일 배정 생성 요청 DTO
 */
@Getter
@Setter
public class TaskAssignmentRequest {
  private Long groupId;
  private String date; // ISO_LOCAL_DATE ("2025-08-05")
  private Long templateId;
  private String assignType; // "MANUAL" or "AUTO"
}
