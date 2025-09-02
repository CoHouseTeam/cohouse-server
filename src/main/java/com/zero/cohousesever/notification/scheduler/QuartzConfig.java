package com.zero.cohousesever.notification.scheduler;

import org.springframework.context.annotation.Configuration;

/**
 * Quartz 전역 설정용 자리.
 * - 본 구조는 "도메인별 스케줄러 서비스에서 동적 Job/Trigger 등록" 방식을 사용합니다.
 * - Boot의 Quartz 오토컨피그를 그대로 활용하므로 여기서는 별도 Bean 정의가 필요 없습니다.
 * - 공통 글로벌 잡이 필요해지면 이 클래스에 추가하세요.
 */
@Configuration
public class QuartzConfig {
    // intentionally empty
}