package com.zero.cohousesever.settlement.dto;

import com.zero.cohousesever.settlement.entity.Settlement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 정산 간단 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementSimpleResponse {
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementSimpleResponse fromEntity(Settlement settlement) {

        return new SettlementSimpleResponse(
                settlement.getTitle(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}

