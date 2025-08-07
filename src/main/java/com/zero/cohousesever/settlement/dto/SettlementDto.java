package com.zero.cohousesever.settlement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SettlementDto {
    private Long id;
    private String category;
    private String title;
    private String description;
    private Long totalAmount;
    private String status;
    private String imageUrl;
    private Long payerId;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}