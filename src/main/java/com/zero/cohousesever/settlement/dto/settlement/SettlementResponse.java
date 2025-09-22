package com.zero.cohousesever.settlement.dto.settlement;

import com.zero.cohousesever.settlement.dto.payment.ParticipantResponse;
import com.zero.cohousesever.settlement.entity.settlement.Settlement;
import com.zero.cohousesever.settlement.entity.settlement.SettlementCategory;
import com.zero.cohousesever.settlement.entity.settlement.SettlementStatus;
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
public class SettlementResponse {
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
    private List<ParticipantResponse> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementResponse fromEntity(Settlement settlement) {
        List<ParticipantResponse> participantResponses = settlement.getSettlementParticipants().stream()
                .map(ParticipantResponse::fromEntity)
                .collect(Collectors.toList());

        return new SettlementResponse(
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
                participantResponses,
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}