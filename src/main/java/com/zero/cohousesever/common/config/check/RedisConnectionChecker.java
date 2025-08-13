package com.zero.cohousesever.common.config.check;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisConnectionChecker implements ApplicationRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equalsIgnoreCase(pong)) { // 문자열 내용이 같으면 대소문자 구분 없이 true 반환
                System.out.println("✅ Redis 연결 성공!");
            } else {
                System.out.println("⚠️  Redis 연결 실패");
            }
        } catch (Exception e) {
            System.out.println("❌ Redis 연결 오류: " + e.getMessage());
        }
    }
}