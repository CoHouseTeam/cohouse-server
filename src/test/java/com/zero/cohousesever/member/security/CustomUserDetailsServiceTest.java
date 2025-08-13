package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(memberRepository);
    }

    @Test
    @DisplayName("존재하는 사용자로 UserDetails 로드 성공")
    void shouldLoadUserByUsernameWhenUserExists() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        String password = "password123";
        Member member = Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUsername()).isEqualTo(email);
        assertThat(customUserDetails.getPassword()).isEqualTo(password);
        assertThat(customUserDetails.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 UserDetails 로드 시 예외 발생")
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(RuntimeException.class); // TODO: loadUserByUsername에서 던진 예외를 캐치하기
    }

    @Test
    @DisplayName("빈 문자열로 UserDetails 로드 시 예외 발생")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // given
        String emptyEmail = "";
        when(memberRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emptyEmail))
                .isInstanceOf(RuntimeException.class); // TODO: loadUserByUsername에서 던진 예외를 캐치하기
    }

    @Test
    @DisplayName("null로 UserDetails 로드 시 예외 발생")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // given
        String nullEmail = null;
        when(memberRepository.findByEmail(nullEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nullEmail))
                .isInstanceOf(RuntimeException.class); // TODO: loadUserByUsername에서 던진 예외를 캐치하기
    }
}
