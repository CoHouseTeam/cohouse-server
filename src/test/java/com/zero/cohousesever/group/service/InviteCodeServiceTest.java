package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.common.utils.RandomCodeGenerator;
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
    private RandomCodeGenerator codeGenerator;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private InviteCodeService inviteCodeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("초대 코드 생성 성공 - 기존 코드가 존재하지 않는 경우")
    void getInviteCode_Success_WithoutExistingCodeByGroupId() {
        // given
        Long groupId = 1L;
        String code = "testcode";
        when(redisTemplate.opsForValue().get("inviteGroupId:" + groupId)).thenReturn(null);
        when(codeGenerator.generateCode(8)).thenReturn(code);

        // when
        String result = inviteCodeService.getInviteCodeByGroupId(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(code);

        verify(valueOperations).set(eq("inviteCode:" + code), eq(groupId.toString()), eq(3L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("inviteGroupId:" + groupId), eq(code), eq(3L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("초대 코드 생성 성공 - 기존 코드가 존재하고 1시간 이상 남은 경우")
    void getInviteCode_Success_WithExistingCodeByGroupId() {
        // given
        Long groupId = 1L;
        String code = "testcode";
        when(redisTemplate.opsForValue().get("inviteGroupId:" + groupId)).thenReturn(code);
        when(redisTemplate.getExpire("inviteCode:" + code, TimeUnit.MINUTES)).thenReturn(120L); // 2시간 남음

        // when
        String result = inviteCodeService.getInviteCodeByGroupId(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(code);

        verify(redisTemplate).expire(eq("inviteCode:" + code), eq(3L), eq(TimeUnit.HOURS));
        verify(redisTemplate).expire(eq("inviteGroupId:" + groupId), eq(3L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("초대 코드 생성 성공 - 기존 코드가 존재하고 1시간 미만 남은 경우")
    void getInviteCode_ByGroupId_Success_WithTtlUnder60() {
        // given
        Long groupId = 1L;
        String code = "testcode";
        when(redisTemplate.opsForValue().get("inviteGroupId:" + groupId)).thenReturn(code);
        when(redisTemplate.getExpire("inviteCode:" + code, TimeUnit.MINUTES)).thenReturn(10L); // 10분 남음
        when(codeGenerator.generateCode(8)).thenReturn(code);

        // when
        String result = inviteCodeService.getInviteCodeByGroupId(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(code);

        verify(redisTemplate, never()).expire(eq("inviteCode:" + code), eq(3L), eq(TimeUnit.HOURS));
        verify(redisTemplate, never()).expire(eq("inviteGroupId:" + groupId), eq(3L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("inviteCode:" + code), eq(groupId.toString()), eq(3L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("inviteGroupId:" + groupId), eq(code), eq(3L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("초대 코드 검증 성공 - 유효한 코드일 경우 groupId 반환")
    void getGroupIdByInviteCode_Success() {
        // given
        String code = "ABCDEFGH";
        Long groupId = 1L;
        when(valueOperations.get("inviteCode:" + code)).thenReturn(groupId.toString());

        // when
        Long result = inviteCodeService.getGroupIdByInviteCode(code);

        // then
        assertThat(result).isEqualTo(groupId);
        verify(valueOperations).get("inviteCode:" + code);
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 잘못된 코드일 경우 예외 발생")
    void getGroupIdByInviteCode_ThrowsException_WhenInvalid() {
        // given
        String code = "INVALID";
        when(valueOperations.get("inviteCode:" + code)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> inviteCodeService.getGroupIdByInviteCode(code))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVITE_CODE_INVALID.getMessage());

        verify(valueOperations).get("inviteCode:" + code);
    }
}
