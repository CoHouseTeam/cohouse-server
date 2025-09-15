package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.utils.RandomCodeGenerator;
import com.zero.cohousesever.member.dto.auth.PasswordForgotRequestDto;
import com.zero.cohousesever.member.dto.auth.PasswordResetRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.zero.cohousesever.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RandomCodeGenerator codeGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 설정
        testMember = Member.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("encodedPassword")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        // Redis ValueOperations Mock 설정
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 설정값 주입
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(passwordResetService, "senderEmail", "noreply@cohouse.com");
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공 - 유효한 이메일, 이름, 활성 상태")
    void sendPasswordResetMail_Success() {
        // given
        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        ReflectionTestUtils.setField(passwordForgotRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(passwordForgotRequestDto, "name", "테스트유저");

        when(memberRepository.findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));

        // when
        passwordResetService.sendPasswordResetMail(passwordForgotRequestDto);

        // then
        verify(memberRepository).findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE);
        verify(valueOperations).set(startsWith("password_reset:"), eq("1"), eq(30L), eq(TimeUnit.MINUTES));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 존재하지 않는 멤버")
    void sendPasswordResetMail_MemberNotFound() {
        // given
        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        ReflectionTestUtils.setField(passwordForgotRequestDto, "email", "none@example.com");
        ReflectionTestUtils.setField(passwordForgotRequestDto, "name", "존재하지 않는 유저");

        when(memberRepository.findByEmailAndNameAndStatus("none@example.com", "존재하지 않는 유저", MemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when
        passwordResetService.sendPasswordResetMail(passwordForgotRequestDto);

        // then
        verify(memberRepository).findByEmailAndNameAndStatus("none@example.com", "존재하지 않는 유저", MemberStatus.ACTIVE);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 이름 불일치")
    void sendPasswordResetMail_NameMismatch() {
        // given
        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        ReflectionTestUtils.setField(passwordForgotRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(passwordForgotRequestDto, "name", "다른이름");

        when(memberRepository.findByEmailAndNameAndStatus("test@example.com", "다른이름", MemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when
        passwordResetService.sendPasswordResetMail(passwordForgotRequestDto);

        // then
        verify(memberRepository).findByEmailAndNameAndStatus("test@example.com", "다른이름", MemberStatus.ACTIVE);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 비활성 멤버")
    void sendPasswordResetMail_InactiveMember() {
        // given
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);

        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        ReflectionTestUtils.setField(passwordForgotRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(passwordForgotRequestDto, "name", "테스트유저");

        when(memberRepository.findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE))
                .thenReturn(Optional.empty()); // ACTIVE 상태가 아닌 경우 조회되지 않음

        // when
        passwordResetService.sendPasswordResetMail(passwordForgotRequestDto);

        // then
        verify(memberRepository).findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 이메일 전송 실패 시 예외 발생하지 않음")
    void sendPasswordResetMail_EmailSendFailure() {
        // given
        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        ReflectionTestUtils.setField(passwordForgotRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(passwordForgotRequestDto, "name", "테스트유저");

        when(memberRepository.findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // when - 예외가 발생하지 않아야 함
        passwordResetService.sendPasswordResetMail(passwordForgotRequestDto);

        // then
        verify(memberRepository).findByEmailAndNameAndStatus("test@example.com", "테스트유저", MemberStatus.ACTIVE);
        verify(valueOperations).set(startsWith("password_reset:"), eq("1"), eq(30L), eq(TimeUnit.MINUTES));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("새 비밀번호 설정 성공")
    void setNewPassword_Success() {
        // given
        String token = "validToken123";
        String redisKey = "password_reset:" + token;

        PasswordResetRequestDto passwordResetRequestDto = new PasswordResetRequestDto();
        ReflectionTestUtils.setField(passwordResetRequestDto, "token", "validToken123");
        ReflectionTestUtils.setField(passwordResetRequestDto, "newPassword", "newPassword123");

        when(valueOperations.get(redisKey)).thenReturn("1");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // when
        passwordResetService.setNewPassword(passwordResetRequestDto);

        // then
        verify(valueOperations).get(redisKey);
        verify(memberRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword123");
        verify(memberRepository).save(testMember);
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("새 비밀번호 설정 실패 - 유효하지 않은 토큰")
    void setNewPassword_InvalidToken() {
        // given
        String token = "invalidToken";
        String redisKey = "password_reset:" + token;

        PasswordResetRequestDto passwordResetRequestDto = new PasswordResetRequestDto();
        ReflectionTestUtils.setField(passwordResetRequestDto, "token", token);
        ReflectionTestUtils.setField(passwordResetRequestDto, "newPassword", "newPassword123");

        when(valueOperations.get(redisKey)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> passwordResetService.setNewPassword(passwordResetRequestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(PASSWORD_RESET_TOKEN_INVALID.getMessage());

        verify(valueOperations).get(redisKey);
        verify(memberRepository, never()).findById(anyLong());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("새 비밀번호 설정 실패 - 멤버 조회 실패")
    void setNewPassword_MemberNotFound() {
        // given
        String token = "validToken123";
        String redisKey = "password_reset:" + token;

        PasswordResetRequestDto passwordResetRequestDto = new PasswordResetRequestDto();
        ReflectionTestUtils.setField(passwordResetRequestDto, "token", "validToken123");
        ReflectionTestUtils.setField(passwordResetRequestDto, "newPassword", "newPassword123");

        when(valueOperations.get(redisKey)).thenReturn("999"); // 존재하지 않는 멤버 ID
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.setNewPassword(passwordResetRequestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_NOT_FOUND.getMessage());

        verify(valueOperations).get(redisKey);
        verify(memberRepository).findById(999L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
        verify(redisTemplate, never()).delete(anyString());
    }
}