package com.zero.cohousesever.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("리프레시 토큰 저장")
    void saveRefreshToken() {
        // given
        Long memberId = 1L;
        String refreshToken = "test-refresh-token";
        long expirationMillis = 86400000L; // 24시간

        // when
        refreshTokenService.saveRefreshToken(memberId, refreshToken, expirationMillis);

        // then
        verify(valueOperations).set("RT:1", refreshToken, expirationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("멤버 ID로 리프레시 토큰 조회")
    void getRefreshToken() {
        // given
        Long memberId = 1L;
        String expectedToken = "test-refresh-token";
        when(valueOperations.get("RT:1")).thenReturn(expectedToken);

        // when
        String result = refreshTokenService.getRefreshToken(memberId);

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(valueOperations).get("RT:1");
    }

    @Test
    @DisplayName("존재하지 않는 멤버 ID로 조회 - null 반환")
    void getRefreshToken_NotFound() {
        // given
        Long memberId = 999L;
        when(valueOperations.get("RT:999")).thenReturn(null);

        // when
        String result = refreshTokenService.getRefreshToken(memberId);

        // then
        assertThat(result).isNull();
        verify(valueOperations).get("RT:999");
    }

    @Test
    @DisplayName("리프레시 토큰 삭제")
    void deleteRefreshToken() {
        // given
        Long memberId = 1L;

        // when
        refreshTokenService.deleteRefreshToken(memberId);

        // then
        verify(stringRedisTemplate).delete("RT:1");
    }

    @Test
    @DisplayName("키 프리픽스 검사")
    void keyPrefixIsCorrect() {
        // given & when
        String keyPrefix = (String) ReflectionTestUtils.getField(refreshTokenService, "REFRESH_TOKEN_KEY_PREFIX");

        // then
        assertThat(keyPrefix).isEqualTo("RT:");
    }

    @Test
    @DisplayName("여러 멤버의 리프레시 토큰 저장")
    void saveMultipleRefreshTokens() {
        // given
        Long memberId1 = 1L;
        Long memberId2 = 2L;
        String token1 = "token-1";
        String token2 = "token-2";
        long expirationMillis = 86400000L;

        // when
        refreshTokenService.saveRefreshToken(memberId1, token1, expirationMillis);
        refreshTokenService.saveRefreshToken(memberId2, token2, expirationMillis);

        // then
        verify(valueOperations).set("RT:1", token1, expirationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        verify(valueOperations).set("RT:2", token2, expirationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("리프레시 토큰 저장 시 만료 시간 검증")
    void saveRefreshTokenWithExpiration() {
        // given
        Long memberId = 1L;
        String refreshToken = "test-token";
        long expirationMillis = 3600000L; // 1시간

        // when
        refreshTokenService.saveRefreshToken(memberId, refreshToken, expirationMillis);

        // then
        verify(valueOperations).set(eq("RT:1"), eq(refreshToken), eq(expirationMillis), eq(java.util.concurrent.TimeUnit.MILLISECONDS));
    }
}
