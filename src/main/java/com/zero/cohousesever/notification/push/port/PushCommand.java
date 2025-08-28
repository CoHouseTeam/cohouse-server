package com.zero.cohousesever.notification.push.port;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class PushCommand {
    private final Long memberId;          // 누구에게
    private final String token;           // 대상 디바이스 토큰(반드시 유효)
    private final String title;           // 제목
    private final String body;            // 본문
    private final Map<String, String> data; // 확장 데이터(선택)
}