package com.zero.cohousesever.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 회원 관련 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    MEMBER_INACTIVE(HttpStatus.UNAUTHORIZED, "이미 탈퇴한 회원입니다."),
    INVALID_SIGNUP_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 회원가입 요청입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

    // 그룹 관련 오류
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹을 찾을 수 없습니다."),
    GROUP_ALREADY_INACTIVE(HttpStatus.NOT_FOUND, "비활성화된 그룹입니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 멤버를 찾을 수 없습니다."),
    GROUP_MEMBER_ALREADY_INACTIVE(HttpStatus.UNAUTHORIZED, "비활성화된 그룹 멤버입니다."),
    NOT_GROUP_LEADER(HttpStatus.FORBIDDEN, "그룹장만 접근할 수 있습니다."),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "해당 그룹 소속이 아닙니다."),
    INVITE_CODE_INVALID(HttpStatus.BAD_REQUEST, "초대 코드가 잘못되었거나 만료되었습니다."),

    // 할일 관련 오류
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 템플릿을 찾을 수 없습니다."),
    REPEAT_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "반복 요일을 찾을 수 없습니다."),
    REPEAT_DAY_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 요일은 이미 등록되어 있습니다."),
    TASK_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "할 일 배정을 찾을 수 없습니다."),
    TASK_ASSIGNMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "할 일 배정이 이미 존재합니다."),
    ASSIGNMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 상태입니다."),
    CANDIDATE_MEMBERS_REQUIRED(HttpStatus.BAD_REQUEST, "후보 멤버 목록이 필요합니다."),
    DATE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "날짜 형식이 유효하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    GROUP_ID_REQUIRED(HttpStatus.BAD_REQUEST,  "그룹 아이디 확인이 필요합니다."),
    ASSIGNMENT_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "할 일 상태 값이 필요합니다."),
    // 게시물 관련 오류
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 게시글입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "본인 게시글만 수정/삭제할 수 있습니다."),
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
