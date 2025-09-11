package com.zero.cohousesever.settlement.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentFailedEvent {
        private final Long settlementId;
        private final Long senderId;
}
