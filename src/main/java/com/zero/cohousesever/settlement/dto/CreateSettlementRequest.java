package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.SettlementCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// 정산 요청 DTO
@Getter
@NoArgsConstructor
public class CreateSettlementRequest {
    private String title;
    private String description;
    private SettlementCategory category;
    private boolean equalDistribution;
    private Long settlementAmount;
    private Map<Long, Long> manualShares; // 참여자 ID별 직접 분배 금액
    private List<Long> participantIds;
}
