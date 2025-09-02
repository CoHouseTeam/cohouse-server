package com.zero.cohousesever.task.dto.repeat;

import com.zero.cohousesever.task.entity.RepeatDay;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import java.util.*;

@Getter
@Builder
public class RepeatDayResponse {
  private Long repeatDayId;
  private Long templateId;
  private String dayOfWeek;

  // 반환 형태
  public static RepeatDayResponse from(RepeatDay rd) {
    return RepeatDayResponse.builder()
        .repeatDayId(rd.getId())
        .templateId(rd.getTaskTemplate().getId())
        .dayOfWeek(rd.getDayOfWeek().name())
        .build();
  }

  // 전체 매핑
  public static List<RepeatDayResponse> fromAll(Collection<RepeatDay> list) {
    return list.stream().map(RepeatDayResponse::from).collect(Collectors.toList());
  }
}
