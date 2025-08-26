package com.zero.cohousesever.tasks.dto.repeat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeatDayRequest {
  private String dayOfWeek;  // "SUNDAY" ~ "SATURDAY"
}
