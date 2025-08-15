package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.SettlementCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// 정산 요청 DTO
@Getter
@NoArgsConstructor
public class CreateSettlementRequest {
    private String title;
    private String description;
    private SettlementCategory category;
    private BigDecimal settlementAmount;
    private List<Long> participantIds;
}
