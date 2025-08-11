package com.zero.cohousesever.settlement.entity;

public enum PaymentStatus {
    WAITING,     // 송금 대기 중 (아직 송금 미완료)
    SENT,        // 송금 완료
    REFUNDED,    // 환불됨 (송금 완료 후 환불된 경우)
    CANCELED,    // 취소됨 (송금 전에 정산 자체가 취소된 경우)
    FAILED       // 송금 과정에서 오류 (실패)
}
