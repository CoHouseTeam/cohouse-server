package com.zero.cohousesever.tasks.dto.repeat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RepeatDayResponse {
  private Long repeatDayId;
  private Long templateId;
  private String dayOfWeek;
}
