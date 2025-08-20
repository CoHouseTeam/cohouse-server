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
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹을 찾을 수 없습니다."),
    GROUP_ALREADY_INACTIVE(HttpStatus.NOT_FOUND, "비활성화된 그룹입니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 멤버를 찾을 수 없습니다."),
    GROUP_MEMBER_ALREADY_INACTIVE(HttpStatus.UNAUTHORIZED, "비활성화된 그룹 멤버입니다."),
    NOT_GROUP_LEADER(HttpStatus.UNAUTHORIZED, "그룹장만 접근할 수 있습니다."),
    INVITE_CODE_INVALID(HttpStatus.BAD_REQUEST, "초대 코드가 잘못되었거나 만료되었습니다."),

    // 할일 관련 오류

    // 게시물 관련 오류

    // 정산 관련 오류

    // 알림 관련 오류

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
