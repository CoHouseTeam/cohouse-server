package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.tasks.entity.enums.AssignmentStatus;
import com.zero.cohousesever.tasks.entity.enums.RepeatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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

  /**
   * 반복 방식 NONE, DAILY, WEEKLY
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RepeatType repeatType;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
