package com.zero.cohousesever.notification.scheduler.support;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 실제 구현: Redis 키 존재로 접속 여부 판단
 * - 키 규칙: presence:member:{memberId}
 * - 게이트웨이/프론트가 접속 시 SET, 종료 시 DEL or TTL 만료
 */
@Component
@RequiredArgsConstructor
public class RedisAppPresenceChecker implements AppPresenceChecker {

    private final StringRedisTemplate redis;

    @Override
    public boolean isOnline(Long memberId) {
        String key = "presence:member:" + memberId;
        Boolean exists = redis.hasKey(key);
        return exists != null && exists;
    }
}