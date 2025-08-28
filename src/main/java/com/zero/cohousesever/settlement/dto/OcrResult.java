package com.zero.cohousesever.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OcrResult {
    private final Long amount;
    private final boolean success;
}
