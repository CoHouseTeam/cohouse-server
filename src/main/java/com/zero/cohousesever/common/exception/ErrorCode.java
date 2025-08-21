package com.zero.cohousesever.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 회원 관련 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 형식입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다."),

    // 그룹 관련 오류
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원의 그룹을 찾을 수 없습니다."),
    NOT_GROUP_LEADER(HttpStatus.FORBIDDEN, "그룹장 권한이 필요합니다."),

    // 할일 관련 오류

    // 게시물 관련 오류

    // 정산 관련 오류
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산 정보를 찾을 수 없습니다."),
    SETTLEMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 정산 내역입니다."),
    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 정산 금액입니다."),
    SETTLEMENT_DATE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 정산 날짜입니다."),
    SETTLEMENT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "정산 권한이 없습니다."),
    SETTLEMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "정산 상태가 유효하지 않습니다."),
    SETTLEMENT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "정산 생성에 실패했습니다."),
    SETTLEMENT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "정산 삭제에 실패했습니다."),
    INVALID_MANUAL_DISTRIBUTION(HttpStatus.BAD_REQUEST, "직접 분배 금액 정보가 올바르지 않습니다."),
    EXCEED_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "분배 금액 합이 총 정산 금액을 초과했습니다."),
    INVALID_PARTICIPANT_COUNT(HttpStatus.BAD_REQUEST, "참여자 수는 1명 이상이어야 합니다."),

    // 송금 관련 오류
    PAYMENT_TRANSFER_FAILED (HttpStatus.INTERNAL_SERVER_ERROR, "송금 처리에 실패했습니다."),

    // 정산 및 송금 히스토리 관련 오류
    PAYMENT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 송금 내역을 찾을 수 없습니다."),
    PAYMENT_HISTORY_ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "이미 환불 처리된 송금 내역입니다."),
    PAYMENT_HISTORY_REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "송금 내역 환불 처리에 실패했습니다."),
    NOT_A_SETTLEMENT_PARTICIPANT(HttpStatus.FORBIDDEN, "정산 참여자가 아닙니다."),
    NOT_THE_SETTLEMENT_PAYER(HttpStatus.FORBIDDEN, "정산 결제자가 아닙니다."),

    // 알림 관련 오류

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
