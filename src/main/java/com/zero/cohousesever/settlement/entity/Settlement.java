package com.zero.cohousesever.settlement.entity;

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

    @Column(name = "total_amount")
    private Long totalAmount;         // 총 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    private String imageUrl;          // 영수증 이미지 URL (optional)

    // TODO MEMBER 추가 시 변경
//    @Column(name = "payer_id")
//    private Long payerId;             // 비용을 낸 사람 ID

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
