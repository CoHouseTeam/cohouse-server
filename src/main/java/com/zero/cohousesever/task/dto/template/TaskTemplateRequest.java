package com.zero.cohousesever.task.dto.template;

import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
public class TaskTemplateRequest {
  private Long groupId;
  private String category;
  private List<String> repeatDays; // 반복요일
  private Boolean randomEnabled;
}
