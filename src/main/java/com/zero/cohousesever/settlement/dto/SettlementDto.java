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
public class SettlementDto {
    private Long id;
    private SettlementCategory category;
    private String title;
    private String description;
    private Long totalAmount;
    private SettlementStatus status;
    private String imageUrl;
    private Long payerId;
    private String payerName;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementDto fromEntity(Settlement settlement) {
        List<ParticipantDto> participantDtos = settlement.getParticipants().stream()
                .map(participant -> new ParticipantDto(
                        participant.getId(),
                        participant.getMember().getId(),
                        participant.getMember().getName(),
                        participant.getShareAmount(),
                        participant.getStatus(),
                        participant.getPaidAt()
                ))
                .collect(Collectors.toList());

        return new SettlementDto(
                settlement.getId(),
                settlement.getCategory(),
                settlement.getTitle(),
                settlement.getDescription(),
                settlement.getSettlementAmount().longValue(),
                settlement.getStatus(),
                settlement.getImageUrl(),
                settlement.getPayer().getId(),
                settlement.getPayer().getName(),
                participantDtos,
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}