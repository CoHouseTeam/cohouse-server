package com.zero.cohousesever.settlement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 거래 히스토리
 */
@Entity
@Table(name = "transaction_histories")
@Getter
@Setter
@NoArgsConstructor
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 송금하는 참여자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Participant payer;

    // 송금 받는 참여자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private Participant payee;

    private Long amount;             // 송금 금액

    private LocalDateTime transferDate;  // 송금 일시

    private String status;           // 상태 (PENDING, COMPLETED, FAILED)
}
