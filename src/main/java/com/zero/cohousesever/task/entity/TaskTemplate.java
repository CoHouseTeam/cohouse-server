package com.zero.cohousesever.task.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TaskTemplate extends BaseEntity {

  @Column(nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private boolean randomEnabled;

}
