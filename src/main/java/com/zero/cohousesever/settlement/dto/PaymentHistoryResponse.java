package com.zero.cohousesever.settlement.dto;


import com.zero.cohousesever.settlement.entity.PaymentHistory;
import com.zero.cohousesever.settlement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 송금 히스토리 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Long id;
    private Long settlementId;
    private Long senderId;
    private Long receiverId;
    private Long amount;
    private PaymentStatus status;
    private LocalDateTime transferAt;

    public static PaymentHistoryResponse fromEntity(PaymentHistory paymentHistory){
        return new PaymentHistoryResponse(
                paymentHistory.getId(),
                paymentHistory.getSettlement().getId(),
                paymentHistory.getSender().getId(),
                paymentHistory.getReceiver().getId(),
                paymentHistory.getAmount(),
                paymentHistory.getStatus(),
                paymentHistory.getTransferDate()
        );
    }
}