package com.zero.cohousesever.task.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "tasks_assignment_histories",
    uniqueConstraints = @UniqueConstraint(name="uk_assignment_date", columnNames = {"assignment_id","date"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentHistory extends BaseEntity {

  @Column(name="assignment_id", nullable=false)
  private Long assignmentId;

  @Column(name="group_member_id", nullable=false)
  private Long groupMemberId;

  @Column(name="category", length=100, nullable=false)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private AssignmentStatus status;

  @Column(name="date", nullable=false)
  private LocalDate date;

}
