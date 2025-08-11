package com.zero.cohousesever.settlement.dto;


import com.zero.cohousesever.settlement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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