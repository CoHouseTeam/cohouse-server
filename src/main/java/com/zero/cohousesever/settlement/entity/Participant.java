package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "settlement_participants")
public class Participant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 배분 금액
    @Column(name = "share_amount", nullable = false)
    private BigDecimal shareAmount; // 배분 금액 소수점일 경우 고려하여 BigDecimal 사용

    // 실제 송금 금액
    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
}