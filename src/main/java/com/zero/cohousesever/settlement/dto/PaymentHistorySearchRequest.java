package com.zero.cohousesever.settlement.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class PaymentHistorySearchRequest {
    private Long groupId;
    private Long settlementId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    // LocalDateTime 변환된 값 사용
    public LocalDateTime getFromDateTime() {
        return fromDate != null ? fromDate.atStartOfDay() : null;
    }

    public LocalDateTime getToDateTime() {
        return toDate != null ? toDate.atTime(LocalTime.MAX) : null;
    }
}
