package com.zero.cohousesever.settlement.dto.settlement;

import com.zero.cohousesever.settlement.entity.settlement.SettlementCategory;
import com.zero.cohousesever.settlement.entity.settlement.SettlementHistory;
import com.zero.cohousesever.settlement.entity.settlement.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 정산 히스토리 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementHistoryResponse {
    private Long id;
    private Long settlementId;
    private String title;
    private SettlementCategory category;
    private Long totalAmount;
    private SettlementStatus status;
    private String payerName;
    private LocalDateTime createdAt;

    public static SettlementHistoryResponse fromEntity(SettlementHistory settlementHistory) {
        return new SettlementHistoryResponse(
                settlementHistory.getId(),
                settlementHistory.getSettlement().getId(),
                settlementHistory.getTitle(),
                settlementHistory.getSettlement().getCategory(),
                settlementHistory.getSettlement().getSettlementAmount(),
                settlementHistory.getStatus(),
                settlementHistory.getSettlement().getPayer().getName(),
                settlementHistory.getCreatedAt()
        );
    }
}
