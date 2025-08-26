package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

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
                .gender(false) // male
                .birthDate(LocalDate.of(2000, 1, 1))
                .alertTime(LocalTime.of(12, 0, 0))
                .profileImageUrl("www.test.com/123")
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

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberService.createMember(name, email, encodedPassword);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 프로필 조회 성공")
    void getMemberProfile_Success() {
        // given
        Long memberId = 1L;

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.of(testMember));

        // when
        MemberProfileSummary result = memberService.getMemberProfile(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testMember.getId());
        assertThat(result.getName()).isEqualTo(testMember.getName());
        assertThat(result.getEmail()).isEqualTo(testMember.getEmail());
        assertThat(result.getGender()).isEqualTo(testMember.getGender() ? "여자" : "남자");
        assertThat(result.getBirthDate()).isEqualTo(testMember.getBirthDate());
        assertThat(result.getAlertTime()).isEqualTo(testMember.getAlertTime());
        assertThat(result.getProfileImageUrl()).isEqualTo(testMember.getProfileImageUrl());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 프로필 조회시 이미 탈퇴한 회원일 경우 예외 발생")
    void getMemberProfile_ThrowsException_WhenInactiveMember() {
        // given
        Long memberId = 1L;
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberProfile(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 정상적으로 상태가 INACTIVE로 변경")
    void deleteMember_Success() {
        // given
        Long memberId = 1L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.deleteMember(memberId);

        // then
        assertThat(testMember.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴시 그룹에 속해있는 경우 예외 발생")
    void deleteMember_ThrowsException_WhenStillInGroup() {
        // given
        Long memberId = 1L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("아직 그룹에 소속된 회원입니다.");

        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(memberRepository, never()).findById(anyLong());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈시 존재하지 않는 회원이면 예외 발생")
    void deleteMember_ThrowsException_WhenMemberNotFound() {
        // given
        Long memberId = 999L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다."); // ErrorCode.MEMBER_NOT_FOUND 메시지

        verify(memberRepository).findById(memberId);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴시 이미 비활성 상태인 경우 예외 발생")
    void deleteMember_ThrowsException_WhenAlreadyInactive() {
        // given
        Long memberId = 1L;
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 탈퇴한 회원입니다.");

        verify(memberRepository, never()).save(any(Member.class));
    }
}
