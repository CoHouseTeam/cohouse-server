package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.PaymentStatus;
import com.zero.cohousesever.settlement.entity.SettlementParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 정산 참여자 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long shareAmount;
    private PaymentStatus status;

    public static ParticipantResponse fromEntity(SettlementParticipant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getMember().getId(),
                participant.getMember().getName(),
                participant.getShareAmount(),
                participant.getStatus()
        );
    }
}