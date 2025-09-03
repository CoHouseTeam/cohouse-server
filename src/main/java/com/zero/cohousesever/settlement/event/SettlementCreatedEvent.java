package com.zero.cohousesever.settlement.event;


import com.zero.cohousesever.settlement.entity.SettlementParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SettlementCreatedEvent {
    private final Long settlementId;
    private final Long groupId;
    private final List<SettlementParticipant> participants;
}
