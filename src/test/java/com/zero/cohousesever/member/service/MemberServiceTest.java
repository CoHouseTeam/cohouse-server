package com.zero.cohousesever.member.service;

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
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

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
    }

    @Test
    @DisplayName("회원 생성 성공 - 정상적인 데이터로 회원 생성")
    void createMember_Success() {
        // given
        String name = "테스트유저";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";

        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // when
        Member result = memberService.createMember(name, email, encodedPassword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 생성 시 저장되는 엔티티 검증")
    void createMember_VerifySavedEntity() {
        // given
        String name = "새로운유저";
        String email = "new@example.com";
        String encodedPassword = "newEncodedPassword";

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            return savedMember;
        });

        // when
        Member result = memberService.createMember(name, email, encodedPassword);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        verify(memberRepository).save(any(Member.class));
    }
}
