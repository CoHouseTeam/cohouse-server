package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "settlement_participants") // 정산 참여자
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    // 배분 금액
    @Column(name = "share_amount", nullable = false)
    private Long shareAmount;

    // 실제 송금 금액
    @Column(name = "paid_amount")
    private Long paidAmount;

    private String status;
}
