package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomUserDetailsTest {

    private Member testMember;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        customUserDetails = new CustomUserDetails(testMember);
    }

    @Test
    @DisplayName("CustomUserDetails 생성 성공")
    void shouldCreateCustomUserDetails() {
        // then
        assertThat(customUserDetails).isNotNull();
        assertThat(customUserDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(customUserDetails.getName()).isEqualTo("테스트 사용자");
        assertThat(customUserDetails.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("getUsername은 이메일을 반환해야 함")
    void shouldReturnEmailAsUsername() {
        // when
        String username = customUserDetails.getUsername();

        // then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("권한 정보 확인 (현재는 빈 권한 정보 반환)")
    void shouldReturnEmptyAuthorities() {
        // when
        Collection<?> authorities = customUserDetails.getAuthorities();

        // then
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("다른 Member로 CustomUserDetails 생성")
    void shouldCreateCustomUserDetailsWithDifferentMember() {
        // given
        Member newMember = Member.builder()
                .name("새 사용자")
                .email("new@example.com")
                .password("newpassword")
                .status(MemberStatus.ACTIVE)
                .build();

        // when
        CustomUserDetails newUserDetails = new CustomUserDetails(newMember);

        // then
        assertThat(newUserDetails.getEmail()).isEqualTo("new@example.com");
        assertThat(newUserDetails.getName()).isEqualTo("새 사용자");
        assertThat(newUserDetails.getPassword()).isEqualTo("newpassword");

        assertThat(newUserDetails.getEmail()).isNotEqualTo(testMember.getEmail());
        assertThat(newUserDetails.getName()).isNotEqualTo(testMember.getName());
        assertThat(newUserDetails.getPassword()).isNotEqualTo(testMember.getPassword());
    }

    @Test
    @DisplayName("null Member로 CustomUserDetails 생성 시 예외 발생")
    void shouldThrowExceptionWhenMemberIsNull() {
        // given
        Member nullMember = null;

        // then
        assertThatThrownBy(() -> new CustomUserDetails(nullMember))
                .isInstanceOf(NullPointerException.class);
    }
}
