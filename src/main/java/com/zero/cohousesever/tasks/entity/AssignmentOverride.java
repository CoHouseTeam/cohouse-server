package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assignment_override_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentOverride {

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
   * 대상자 (요청 받은 사람 1 or 다수 or 전체)
   */
  @Column(nullable = false)
  private Long targetId;

  private Long modifierId; // 최종 변경자

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
