package com.zero.cohousesever.notification.dto; // 패키지 선언: 프로젝트 DTO가 모여있는 위치

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미읽음(미확인) 알림 개수를 내려주는 응답 DTO 클래스입니다.
 * - 배지 숫자 표시에 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class UnreadCountResponse {

    private long count;
}