package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 송금 히스토리
 */
@Table(name = "payment_histories")
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender; // 송금하는 정산 참여자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver; // 송금 받는 정산 참여자

    private Long amount;             // 송금 금액

    private LocalDateTime transferDate;  // 송금 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
}
