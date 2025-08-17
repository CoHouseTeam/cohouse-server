package com.zero.cohousesever.member.service;

import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.dto.auth.LoginRequestDto;
import com.zero.cohousesever.member.dto.auth.SignupRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Member testMember;
    private SignupRequestDto signupRequestDto;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        // 멤버 엔티티
        testMember = Member.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("encodedPassword")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        // 회원가입 요청 DTO
        signupRequestDto = new SignupRequestDto();
        ReflectionTestUtils.setField(signupRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(signupRequestDto, "name", "테스트유저");
        ReflectionTestUtils.setField(signupRequestDto, "password", "password123");
        ReflectionTestUtils.setField(signupRequestDto, "passwordRepeat", "password123");

        // 로그인 요청 DTO
        loginRequestDto = new LoginRequestDto();
        ReflectionTestUtils.setField(loginRequestDto, "email", "test@example.com");
        ReflectionTestUtils.setField(loginRequestDto, "password", "password123");
    }

    @Test
    @DisplayName("회원가입 성공 - 정상적인 데이터로 회원가입")
    void registerMember_Success() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberService.createMember(anyString(), anyString(), anyString())).thenReturn(testMember);

        // when
        Member result = authService.registerMember(signupRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("테스트유저");

        verify(memberRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(memberService).createMember("테스트유저", "test@example.com", "encodedPassword");
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호와 비밀번호 확인이 일치하지 않음")
    void registerMember_Failure_PasswordMismatch() {
        // given
        SignupRequestDto invalidPasswordDto = new SignupRequestDto();
        ReflectionTestUtils.setField(invalidPasswordDto, "email", "test@example.com");
        ReflectionTestUtils.setField(invalidPasswordDto, "name", "테스트유저");
        ReflectionTestUtils.setField(invalidPasswordDto, "password", "password123");
        ReflectionTestUtils.setField(invalidPasswordDto, "passwordRepeat", "differentPassword");

        // when & then
        assertThatThrownBy(() -> authService.registerMember(invalidPasswordDto))
                .isInstanceOf(RuntimeException.class); // TODO: 적절한 예외 처리 로직 작성 후 메시지 검증 추가

        verify(memberRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberService, never()).createMember(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void registerMember_Failure_DuplicateEmail() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.registerMember(signupRequestDto))
                .isInstanceOf(RuntimeException.class); // TODO: 적절한 예외 처리 로직 작성 후 메시지 검증 추가

        verify(memberRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberService, never()).createMember(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 성공 - 정상적인 이메일과 비밀번호")
    void loginAuthenticate_Success() {
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refreshToken");

        // when
        JwtTokenResponseDto result = authService.loginAuthenticate(loginRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");

        verify(memberRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateAccessToken("test@example.com", "테스트유저");
        verify(jwtTokenProvider).generateRefreshToken();
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginAuthenticate_Failure_EmailNotFound() {
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.loginAuthenticate(loginRequestDto))
                .isInstanceOf(RuntimeException.class); // TODO: 적절한 예외 처리 로직 작성 후 메시지 검증 추가

        verify(memberRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateRefreshToken();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginAuthenticate_Failure_WrongPassword() {
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.loginAuthenticate(loginRequestDto))
                .isInstanceOf(RuntimeException.class); // TODO: 적절한 예외 처리 로직 작성 후 메시지 검증 추가

        verify(memberRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateRefreshToken();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복되지 않은 이메일")
    void isEmailDuplicated_False() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);

        // when
        boolean result = authService.isEmailDuplicated("new@example.com");

        // then
        assertThat(result).isFalse();
        verify(memberRepository).existsByEmail("new@example.com");
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복된 이메일")
    void isEmailDuplicated_True() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        boolean result = authService.isEmailDuplicated("existing@example.com");

        // then
        assertThat(result).isTrue();
        verify(memberRepository).existsByEmail("existing@example.com");
    }
}
