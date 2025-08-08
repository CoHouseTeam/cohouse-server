package com.zero.cohousesever.settlement.entity;

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
public class SettlementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    private LocalDateTime changedAt;

    private String changedBy; // 변경자 정보

    @Column(columnDefinition = "TEXT")
    private String details;
}
