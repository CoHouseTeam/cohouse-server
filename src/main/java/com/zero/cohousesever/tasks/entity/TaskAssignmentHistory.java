package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.tasks.entity.enums.CompletionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "tasks_assignment_histories",
    uniqueConstraints = @UniqueConstraint(name="uk_assignment_date", columnNames = {"assignment_id","date"})
)
@Getter @Setter
public class TaskAssignmentHistory {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="assignment_id", nullable=false)
  private Long assignmentId;

  @Column(name="group_member_id", nullable=false)
  private Long groupMemberId;

  @Column(name="category", length=100, nullable=false)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private CompletionStatus status; // COMPLETED / NOT_COMPLETED

  @Column(name="date", nullable=false)
  private LocalDate date; // ← 대문자 D 오타 수정

  @Column(name="created_at", nullable=false)
  private LocalDateTime createdAt;

  @Column(name="updated_at", nullable=false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    final LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

}
