package com.zero.cohousesever.notification.push.port;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * - 푸시 발송에 필요한 최소 파라미터 묶음.
 * - 멤버ID(로깅/추적), 대상 토큰, 타이틀, 바디, 데이터(선택)를 포함.
 */
@Getter
@Builder
public class PushCommand {
    private final Long memberId;          // 누구에게
    private final String token;           // 대상 디바이스 토큰
    private final String title;           // 제목
    private final String body;            // 본문
    private final Map<String, String> data; // 확장 데이터(선택)
}