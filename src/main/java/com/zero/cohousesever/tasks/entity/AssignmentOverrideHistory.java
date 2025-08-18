package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks_override_request_histories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AssignmentOverrideHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 요청자, 어느 교체 요청의 결과인지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "request_id", nullable = false)
  private AssignmentOverride request;

  // 대상자
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  // 최종 변경자(보통 수락한 대상자와 동일)
  @Column(name = "modifier_id", nullable = false)
  private Long modifierId;

  // 공용 게시판 알림글 ID
  @Column(name = "post_id", nullable = false)
  private Long postId;

  // ACCEPTED / REJECTED
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OverrideStatus status;

  // 요청 시각
  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  // 응답 시각
  @Column(name = "responded_at", nullable = false)
  private LocalDateTime respondedAt;

  @PrePersist
  void prePersist() {
    // 히스토리는 "결과 기록"이므로 respondedAt은 보통 서비스에서 채워 넣음.
    if (this.requestedAt == null) this.requestedAt = LocalDateTime.now();
    if (this.respondedAt == null) this.respondedAt = LocalDateTime.now();
  }
}
