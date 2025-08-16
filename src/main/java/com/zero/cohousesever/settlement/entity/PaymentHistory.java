package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 송금 히스토리
 */
@Entity
@Table(name = "payment_histories")
@Getter
@Setter
@NoArgsConstructor
public class PaymentHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Participant sender; // 송금하는 정산 참여자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Participant receiver; // 송금 받는 정산 참여자

    private Long amount;             // 송금 금액

    private LocalDateTime transferDate;  // 송금 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    public static PaymentHistory fromParticipants(Settlement settlement, Participant sender, Participant receiver, Long amount) {
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setSettlement(settlement);
        paymentHistory.setSender(sender);
        paymentHistory.setReceiver(receiver);
        paymentHistory.setAmount(amount);
        paymentHistory.setTransferDate(LocalDateTime.now());
        paymentHistory.setStatus(PaymentStatus.PAID);
        return paymentHistory;
    }
}
