package com.zero.cohousesever.settlement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlements") // 테이블명도 맞춰서 변경
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;          // 분류 (예: 식사, 교통비 등)
    private String title;             // 제목 (예: 배달비)
    private String description;       // 상세 설명 (optional)

    @Column(name = "total_amount")
    private Long totalAmount;         // 총 금액

    private String status;            // 상태 (예: PENDING, COMPLETE 등)

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
