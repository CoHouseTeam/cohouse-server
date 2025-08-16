package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "settlements")
public class Settlement extends BaseEntity {
    @Column(nullable = false)
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private SettlementCategory category;

    @Column(nullable = false)
    private boolean isEqualDistribution;  // true: 균등 분배, false: 개별 금액 분배

    @Column(nullable = false)
    private Long settlementAmount; // 정산 금액

    @Column(nullable = false)
    private Long platformSupportAmount;  // 플랫폼이 지원하는 오차 금액

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
