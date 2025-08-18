package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 정산 참여자 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private Long id;
    private Long memberId;
    private String memberName;
    private BigDecimal shareAmount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
}