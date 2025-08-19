package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.SettlementCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

// 정산 요청 DTO
@Getter
@Setter
@Builder
public class CreateSettlementRequest {
    private String title;
    private String description;
    private SettlementCategory category;
    private boolean equalDistribution;
    private Long settlementAmount;
    private Map<Long, Long> manualShares; // 참여자 ID별 직접 분배 금액
    private List<Long> participantIds;
}
