package com.zero.cohousesever.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteRequest {
    private Long participantId;
    private BigDecimal paidAmount;
    private LocalDateTime paidAt;
    private String note;
}