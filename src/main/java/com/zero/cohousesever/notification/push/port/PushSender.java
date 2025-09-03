package com.zero.cohousesever.notification.push.port;

import com.zero.cohousesever.common.exception.CustomException;

/**
 * - “푸시를 보낸다” 라는 동작을 추상화하는 포트.
 * - 구현체(Firebase, 다른 벤더)를 갈아끼우더라도 서비스 코드는 동일 인터페이스만 의존.
 */
public interface PushSender {

    /**
     * - 입력 커맨드를 바탕으로 대상 토큰에 푸시를 전송한다.
     * - 예외는 프로젝트 공통 예외로 래핑하여 던진다.
     */
    void send(PushCommand command) throws CustomException;
}