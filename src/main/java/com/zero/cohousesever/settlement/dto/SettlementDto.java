package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.SettlementCategory;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 정산 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDto {
    private Long id;
    private SettlementCategory category;
    private String title;
    private String description;
    private Long totalAmount;
    private SettlementStatus status;
    private String imageUrl;
    private Long payerId;
    private String payerName;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}