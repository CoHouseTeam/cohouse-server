package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 정산 히스토리
 */
@Entity
@Table(name = "settlement_histories")
@Getter
@Setter
@NoArgsConstructor
public class SettlementHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @Column(nullable = false)
    private String title; // 정산 제목

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_member_id")
    private Member changedBy; // 변경자 정보
}
