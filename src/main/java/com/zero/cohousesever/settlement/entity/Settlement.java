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

    private String status;            // 상태 (PENDING, COMPLETE)

    private String imageUrl;          // 영수증 이미지 URL (optional)

    @Column(name = "payer_id")
    private Long payerId;             // 비용을 낸 사람 ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 생성 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 수정 시각

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 완료 시각 (optional)

    // 정산 참여자 목록
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Participant> participants;
}
