package com.zero.cohousesever.settlement.dto;


import com.zero.cohousesever.settlement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 송금 히스토리 응답 DTO
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Long paymentHistoryId;
    private Long settlementId;
    private Long participantId;
    private Long amount;
    private PaymentStatus status;
    private String paymentType;
    private LocalDateTime processedAt;
}