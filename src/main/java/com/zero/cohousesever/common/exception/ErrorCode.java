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

    // 할일 관련 오류
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 템플릿을 찾을 수 없습니다."),
    REPEAT_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 반복 요일을 찾을 수 없습니다."),
    CANDIDATE_MEMBERS_REQUIRED(HttpStatus.BAD_REQUEST, "배정 후보자 목록이 필요합니다."),
    DATE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 형식입니다. (yyyy-MM-dd)"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    TASK_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 할일 배정을 찾을 수 없습니다."),

    OVERRIDE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "담당자 변경 요청을 찾을 수 없습니다."),
    OVERRIDE_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    OVERRIDE_PAST_DATE_FORBIDDEN(HttpStatus.BAD_REQUEST, "과거 날짜의 할일은 처리할 수 없습니다."),
    OVERRIDE_REQUESTER_MUST_BE_ASSIGNEE(HttpStatus.FORBIDDEN, "현재 담당자만 요청을 생성할 수 있습니다."),
    OVERRIDE_ACCEPTOR_MUST_BE_TARGET(HttpStatus.FORBIDDEN, "요청 대상자만 응답할 수 있습니다."),
    OVERRIDE_BROADCAST_REJECT_FORBIDDEN(HttpStatus.FORBIDDEN, "브로드캐스트 요청은 거절할 수 없습니다."),
    OVERRIDE_NOT_SAME_GROUP(HttpStatus.FORBIDDEN, "같은 그룹의 그룹멤버만 가능합니다."),
    OVERRIDE_SWAP_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "스왑 대상 할일 배정을 찾을 수 없습니다."),
    OVERRIDE_SWAP_DIFFERENT_GROUP(HttpStatus.FORBIDDEN, "서로 변경은 같은 그룹 내에서만 가능합니다."),
    // 게시물 관련 오류

    // 정산 관련 오류

    // 알림 관련 오류

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
