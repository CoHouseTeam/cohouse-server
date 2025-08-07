package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_override_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentOverrideRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 변경 대상 할일
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignment_id", nullable = false)
  private TaskAssignment assignment;

  /**
   * 요청자 (기존 담당자)
   */
  @Column(nullable = false)
  private Long requesterId;

  /**
   * 요청 수신자 (변경될 담당자)
   */
  @Column(nullable = false)
  private Long receiverId;

  /**
   * 요청 상태
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OverrideStatus status;

  private LocalDateTime requestedAt;
  private LocalDateTime respondedAt;

  @PrePersist
  public void prePersist() {
    this.requestedAt = LocalDateTime.now();
    if (this.status == null) {
      this.status = OverrideStatus.REQUESTED;
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.respondedAt = LocalDateTime.now();
  }
}
