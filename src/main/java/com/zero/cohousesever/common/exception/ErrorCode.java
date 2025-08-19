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
    REPEAT_DAY_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 요일은 이미 존재합니다."),
    TASK_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 할일 배정을 찾을 수 없습니다."),
    TASK_ASSIGNMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜에 동일 템플릿 배정이 이미 생성되었습니다."),
    ASSIGNMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 할일 상태입니다."),
    CANDIDATE_MEMBERS_REQUIRED(HttpStatus.BAD_REQUEST, "배정 후보자 목록이 필요합니다."),
    DATE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 형식입니다. (yyyy-MM-dd)"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    // 게시물 관련 오류

    // 정산 관련 오류

    // 알림 관련 오류

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
