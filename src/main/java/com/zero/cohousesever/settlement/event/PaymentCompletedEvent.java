package com.zero.cohousesever.settlement.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCompletedEvent {
        private final Long settlementId;
        private final Long groupId;
        private final Long senderId;
}
