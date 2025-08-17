package com.zero.cohousesever.group.service;

import com.zero.cohousesever.group.dto.group.GroupNameDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.enums.GroupStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupService groupService;

    private Member testMember;
    private Group testGroup;
    private GroupMember testGroupMember;
    private GroupNameDto groupNameDto;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        testMember = Member.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testGroup = Group.builder()
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testGroup, "id", 1L);

        testGroupMember = GroupMember.builder()
                .member(testMember)
                .nickname("테스트 사용자")
                .isLeader(true)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testGroupMember, "id", 1L);

        groupNameDto = new GroupNameDto();
        ReflectionTestUtils.setField(groupNameDto, "groupName", "테스트 그룹");
    }

    @Test
    @DisplayName("그룹 생성 성공 테스트")
    void createGroup_Success() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // when
        GroupSummary result = groupService.createGroup(memberId, groupNameDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 그룹");
        assertThat(result.getStatus()).isEqualTo(GroupStatus.ACTIVE);

        verify(memberRepository).findById(memberId);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("존재하지 않는 멤버로 그룹 생성 시 예외 발생 테스트")
    void createGroup_ThrowsException_WhenMemberNotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, // TODO: 적절한 예외 던지기
                () -> groupService.createGroup(memberId, groupNameDto)
        );

        verify(memberRepository, times(1)).findById(memberId);
        verify(groupRepository, never()).save(any(Group.class));
        verify(groupMemberRepository, never()).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("그룹 생성 시 그룹 멤버가 올바르게 설정되는지 테스트")
    void createGroup_GroupMemberCorrectlyConfigured() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // when
        GroupSummary result = groupService.createGroup(memberId, groupNameDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupMembers()).isNotNull();
        assertThat(result.getGroupMembers()).hasSize(1);
        assertThat(result.getGroupMembers().get(0).getMemberId()).isEqualTo(1L);
        assertThat(result.getGroupMembers().get(0).getIsLeader()).isEqualTo(true);

        verify(groupRepository).save(any(Group.class));
    }
}
