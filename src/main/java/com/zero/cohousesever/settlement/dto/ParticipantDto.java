package com.zero.cohousesever.settlement.dto;

import java.time.LocalDateTime;

public class ParticipantDto {
    private Long id;
    private Long groupMemberId;
    private Long perPersonAmount;
    private String status;
    private LocalDateTime paidAt;
}