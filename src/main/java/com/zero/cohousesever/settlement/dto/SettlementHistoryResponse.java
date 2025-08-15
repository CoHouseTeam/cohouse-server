package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.SettlementCategory;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
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
    private String title;
    private SettlementCategory category;
    private Long totalAmount;
    private SettlementStatus status;
    private String payerName;
    private LocalDateTime createdAt;
}
