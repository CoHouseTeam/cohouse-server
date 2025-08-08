package com.zero.cohousesever.settlement.entity;

public enum TransactionStatus {
    PENDING,      // 대기 중
    COMPLETED,    // 이체 완료됨
    REFUNDED,     // 환불됨
    CANCELLED,    // 취소됨
    FAILED
}
