package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "settlements")
public class Settlement extends BaseEntity {
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private SettlementCategory category;

    private BigDecimal settlementAmount; // 정산 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private String imageUrl; // 영수증 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member payer;           // 정산 결제자

    private LocalDateTime completedAt; // 정산 완료일시

    // 정산 참여자 목록
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Participant> participants;
}
