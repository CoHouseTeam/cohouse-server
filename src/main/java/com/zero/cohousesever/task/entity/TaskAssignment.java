package com.zero.cohousesever.task.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
    name = "task_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_task_template_date",
        columnNames = {"template_id", "date"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment extends BaseEntity {

  /**
   * 어떤 템플릿 할일인지
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private TaskTemplate template;

  @Column(nullable = false)
  private Long groupMemberId;

  @Column(nullable = false)
  private LocalDate date;

  /**
   * 현재 상태 PENDING, COMPLETED 등
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssignmentStatus status;

  @PrePersist
  public void prePersist() {
    if (this.status == null) {
      this.status = AssignmentStatus.PENDING;
    }
  }

}
