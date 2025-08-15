package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.enums.TokenValidationStatus;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공")
    void shouldAuthenticateWithValidJwtToken() throws ServletException, IOException {
        // given
        String validToken = "valid.jwt.token";
        String email = "test@example.com";

        Member member = createTestMember();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        request.addHeader("Authorization", "Bearer " + validToken);

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(TokenValidationStatus.VALID);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
        verify(customUserDetailsService).loadUserByUsername(email);

        // SecurityContext에 인증 정보가 설정되었는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        
        // 필터 체인이 정상적으로 실행되었는지 확인
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("만료된 JWT 토큰으로 인증 실패")
    void shouldNotAuthenticateWithExpiredJwtToken() throws ServletException, IOException {
        // given
        String expiredToken = "expired.jwt.token";
        request.addHeader("Authorization", "Bearer " + expiredToken);

        when(jwtTokenProvider.validateToken(expiredToken)).thenReturn(TokenValidationStatus.EXPIRED);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(expiredToken);
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        // 401 Unauthorized 응답 확인
        assertThat(response.getStatus()).isEqualTo(401);
        
        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 인증 실패")
    void shouldNotAuthenticateWithInvalidJwtToken() throws ServletException, IOException {
        // given
        String invalidToken = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(TokenValidationStatus.INVALID);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        // 401 Unauthorized 응답 확인
        assertThat(response.getStatus()).isEqualTo(401);
        
        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("JWT 토큰이 없는 경우 인증 실패")
    void shouldContinueFilterChainWhenNoJwtToken() throws ServletException, IOException {
        // given
        // Authorization 헤더 없음
        when(jwtTokenProvider.validateToken(null)).thenReturn(TokenValidationStatus.INVALID);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        // 401 Unauthorized 응답 확인
        assertThat(response.getStatus()).isEqualTo(401);

        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("잘못된 형식의 Authorization 헤더")
    void shouldHandleMalformedAuthorizationHeader() throws ServletException, IOException {
        // given
        request.addHeader("Authorization", "InvalidFormat");
        when(jwtTokenProvider.validateToken(null)).thenReturn(TokenValidationStatus.INVALID);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        // 401 Unauthorized 응답 확인
        assertThat(response.getStatus()).isEqualTo(401);

        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("토큰 검증 후 사용자 정보를 찾을 수 없는 경우")
    void shouldHandleUserNotFoundAfterTokenValidation() throws ServletException, IOException {
        // given
        String validToken = "valid.jwt.token";
        String email = "nonexistent@example.com";

        request.addHeader("Authorization", "Bearer " + validToken);

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(TokenValidationStatus.VALID);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email))
                .thenThrow(new RuntimeException("User not found"));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
        verify(customUserDetailsService).loadUserByUsername(email);

        // 401 Unauthorized 응답 확인
        assertThat(response.getStatus()).isEqualTo(401);
        
        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // 테스트용 멤버 엔티티 생성
    private Member createTestMember() {
        return Member.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
