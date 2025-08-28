package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private InviteCodeService inviteCodeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("초대 코드 생성 성공 - 8자리 코드 생성 및 Redis 저장 확인")
    void generateInviteCode_Success() {
        // given
        Long groupId = 1L;
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // when
        String code = inviteCodeService.generateInviteCode(groupId);

        // then
        assertThat(code).isNotNull();
        assertThat(code.length()).isEqualTo(8); // 8자리 코드인지 검증

        verify(redisTemplate).hasKey("inviteCode:" + code);
        verify(valueOperations).set(eq("inviteCode:" + code), eq(groupId.toString()), eq(3L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("초대 코드 검증 성공 - 유효한 코드일 경우 groupId 반환")
    void validateInviteCode_Success() {
        // given
        String code = "ABCDEFGH";
        Long groupId = 1L;
        when(valueOperations.get("inviteCode:" + code)).thenReturn(groupId.toString());

        // when
        Long result = inviteCodeService.validateInviteCode(code);

        // then
        assertThat(result).isEqualTo(groupId);
        verify(valueOperations).get("inviteCode:" + code);
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 잘못된 코드일 경우 예외 발생")
    void validateInviteCode_ThrowsException_WhenInvalid() {
        // given
        String code = "INVALID";
        when(valueOperations.get("inviteCode:" + code)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> inviteCodeService.validateInviteCode(code))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVITE_CODE_INVALID.getMessage());

        verify(valueOperations).get("inviteCode:" + code);
    }

    @Test
    @DisplayName("초대 코드 생성 시 중복 코드가 있으면 새로운 코드로 재생성된다")
    void generateInviteCode_withCollision() {
        // given
        Long groupId = 1L;

        // 첫 번째 hasKey 호출 → true (충돌 발생), 두 번째 → false (정상)
        when(redisTemplate.hasKey(anyString()))
                .thenReturn(true)   // 첫 번째 시도 충돌
                .thenReturn(false); // 두 번째 시도 성공

        // when
        String code = inviteCodeService.generateInviteCode(groupId);

        // then
        assertThat(code).isNotNull();
        verify(redisTemplate, times(2)).hasKey(anyString()); // 최소 2번 호출됨
        verify(valueOperations, times(1))
                .set(startsWith("inviteCode:"), eq(groupId.toString()), anyLong(), eq(TimeUnit.HOURS));
    }
}
