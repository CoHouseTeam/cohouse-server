package com.zero.cohousesever.settlement.entity;

/**
 * 정산 상태
 */
public enum SettlementStatus {
    PENDING,    // 정산 진행 중
    COMPLETED,  // 모든 송금 완료
    CANCELED    // 정산 취소됨
}
