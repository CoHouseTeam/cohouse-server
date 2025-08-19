package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.entity.SettlementCategory;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 정산 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponseDto {
    private Long id;
    private SettlementCategory category;
    private String title;
    private String description;
    private Long settlementAmount;
    private SettlementStatus status;
    private String imageUrl;
    private Long payerId;
    private String payerName;
    private Long platformSupportAmount;
    private boolean equalDistribution;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementResponseDto fromEntity(Settlement settlement) {
        List<ParticipantDto> participantDtos = settlement.getSettlementParticipants().stream()
                .map(ParticipantDto::fromEntity)
                .collect(Collectors.toList());

        return new SettlementResponseDto(
                settlement.getId(),
                settlement.getCategory(),
                settlement.getTitle(),
                settlement.getDescription(),
                settlement.getSettlementAmount(),
                settlement.getStatus(),
                settlement.getImageUrl(),
                settlement.getPayer().getId(),
                settlement.getPayer().getName(),
                settlement.getPlatformSupportAmount(),
                settlement.isEqualDistribution(),
                participantDtos,
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}