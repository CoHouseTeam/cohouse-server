package com.zero.cohousesever.common.config.check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConnectionChecker implements ApplicationRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("✅ Redis 연결 성공!");
        } catch (Exception e) {
            log.error("❌ Redis 연결 실패", e);
        }
    }
}