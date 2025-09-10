package com.zero.cohousesever.settlement.dto.payment;

import com.zero.cohousesever.settlement.entity.payment.PaymentStatus;
import com.zero.cohousesever.settlement.entity.settlement.SettlementParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 정산 참여자 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long memberId;
    private String memberName;
    private Long shareAmount;
    private PaymentStatus status;

    public static ParticipantResponse fromEntity(SettlementParticipant participant) {
        return new ParticipantResponse(
                participant.getMember().getId(),
                participant.getMember().getName(),
                participant.getShareAmount(),
                participant.getStatus()
        );
    }
}