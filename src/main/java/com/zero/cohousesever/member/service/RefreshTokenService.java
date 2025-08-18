package com.zero.cohousesever.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "RT:";

    private final RedisTemplate<String, String> stringRedisTemplate;

    public void saveRefreshToken(Long memberId, String refreshToken, long expirationMillis) {
        // "RT:{memberId}" 형태로 키 저장
        String key = REFRESH_TOKEN_KEY_PREFIX + memberId;

        stringRedisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + memberId;

        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + memberId;

        stringRedisTemplate.delete(key);
    }
}
