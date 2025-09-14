package com.zero.cohousesever.settlement.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OcrResult {
    private final Long amount;
    private final boolean success;
}
