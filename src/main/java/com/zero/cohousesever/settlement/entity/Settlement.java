package com.zero.cohousesever.settlement.entity;

import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "settlements")
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;
    private String description;

    @Column(name = "settlement_amount")
    private Long settlementAmount; // 정산 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    private String imageUrl;          // 영수증 이미지 URL (optional)

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "payer_id", nullable = false)
    private Member payerId;           // 비용을 낸 사람 ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 완료 시각 (optional)

    // 정산 참여자 목록
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Participant> participants;
}
