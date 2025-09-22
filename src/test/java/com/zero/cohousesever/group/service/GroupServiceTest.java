package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.group.dto.group.GroupInviteDto;
import com.zero.cohousesever.group.dto.group.GroupJoinDto;
import com.zero.cohousesever.group.dto.group.GroupCreateDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
import com.zero.cohousesever.group.dto.groupmember.IsLeaderDto;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferRequestDto;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferResponseDto;
import com.zero.cohousesever.group.dto.groupmember.GroupMemberSummary;
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
import java.util.List;
import java.util.Optional;

import static com.zero.cohousesever.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private InviteCodeService inviteCodeService;

    private Member testMember;
    private Group testGroup;
    private GroupMember testGroupMember;
    private GroupCreateDto groupCreateDto;

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
                .group(testGroup)
                .nickname("테스트 사용자")
                .isLeader(true)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testGroupMember, "id", 1L);

        groupCreateDto = new GroupCreateDto();
        ReflectionTestUtils.setField(groupCreateDto, "groupName", "테스트 그룹");
        ReflectionTestUtils.setField(groupCreateDto, "leaderNickname", "리더 닉네임");
    }

    @Test
    @DisplayName("그룹 생성 성공 테스트")
    void createGroup_Success() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // when
        GroupSummary result = groupService.createGroup(memberId, groupCreateDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 그룹");
        assertThat(result.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        assertThat(result.getGroupMembers().size()).isEqualTo(1);

        verify(memberRepository).findById(memberId);
        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("존재하지 않는 멤버로 그룹 생성 시 예외 발생 테스트")
    void createGroup_ThrowsException_WhenMemberNotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(memberId, groupCreateDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_NOT_FOUND.getMessage());

        verify(memberRepository).findById(memberId);
        verify(groupRepository, never()).save(any(Group.class));
        verify(groupMemberRepository, never()).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("그룹 생성 시 이미 그룹에 속해있는 경우 예외 발생 테스트")
    void createGroup_ThrowsException_WhenMemberAlreadyInGroup() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(memberId, groupCreateDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ALREADY_IN_GROUP.getMessage());

        verify(memberRepository).findById(memberId);
        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(groupRepository, never()).save(any(Group.class));
        verify(groupMemberRepository, never()).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("그룹 생성 시 그룹 멤버가 올바르게 설정되는지 테스트")
    void createGroup_GroupMemberCorrectlyConfigured() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // when
        GroupSummary result = groupService.createGroup(memberId, groupCreateDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupMembers()).isNotNull();
        assertThat(result.getGroupMembers()).hasSize(1);
        assertThat(result.getGroupMembers().get(0).getMemberId()).isEqualTo(1L);
        assertThat(result.getGroupMembers().get(0).getIsLeader()).isEqualTo(true);

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("멤버 ID로 그룹 조회 성공 테스트")
    void getGroupByMemberId_Success() {
        // given
        Long memberId = 1L;
        when(groupMemberRepository.findByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when
        GroupSummary result = groupService.getGroupByMemberId(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testGroup.getId());
        assertThat(result.getName()).isEqualTo(testGroup.getName());

        verify(groupMemberRepository).findByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("내 그룹 조회 시 활성 그룹 멤버가 없을 때 예외 발생 테스트")
    void getGroupByMemberId_ThrowsException_WhenActiveGroupMemberNotFound() {
        // given
        Long memberId = 999L;
        when(groupMemberRepository.findByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.getGroupByMemberId(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_MEMBER_NOT_FOUND.getMessage());

        verify(groupMemberRepository).findByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹 조회 성공 테스트")
    void getGroup_Success() {
        // given
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));

        // when
        GroupSummary result = groupService.getGroup(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 그룹");
        assertThat(result.getStatus()).isEqualTo(GroupStatus.ACTIVE);

        verify(groupRepository).findById(groupId);
    }

    @Test
    @DisplayName("존재하지 않는 그룹 조회 시 예외 발생 테스트")
    void getGroup_ThrowsException_WhenGroupNotFound() {
        // given
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.getGroup(groupId))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_NOT_FOUND.getMessage());

        verify(groupRepository).findById(groupId);
    }


    @Test
    @DisplayName("그룹 수정 성공 테스트")
    void updateGroup_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        // 그룹장 설정
        testGroupMember = GroupMember.builder()
                .member(testMember)
                .nickname("테스트 사용자")
                .isLeader(true)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testGroupMember, "id", 1L);

        GroupSummary requestDto = GroupSummary.builder()
                .id(groupId)
                .name("수정된 그룹 이름")
                .status(GroupStatus.ACTIVE)
                .build();

        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)).thenReturn(Optional.of(testGroupMember));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        GroupSummary result = groupService.updateGroup(memberId, groupId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 그룹 이름");
        verify(groupMemberRepository).findByMemberIdAndGroupId(memberId, groupId);
        verify(groupRepository).findById(groupId);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹장이 아닌 그룹원이 그룹 수정 시 예외 발생")
    void updateGroup_ThrowsException_WhenNotLeader() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        // 그룹원이지만 리더 아님
        ReflectionTestUtils.setField(testGroupMember, "isLeader", false);

        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)).thenReturn(Optional.of(testGroupMember));

        GroupSummary requestDto = GroupSummary.builder()
                .id(groupId)
                .name("수정된 그룹 이름")
                .status(GroupStatus.ACTIVE)
                .build();

        // when & then
        assertThatThrownBy(() -> groupService.updateGroup(memberId, groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_GROUP_LEADER.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupId(memberId, groupId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹 수정 시 그룹 멤버가 존재하지 않는 경우 예외 발생")
    void updateGroup_ThrowsException_WhenGroupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 999L;

        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)).thenReturn(Optional.empty());

        GroupSummary requestDto = GroupSummary.builder()
                .id(groupId)
                .name("수정된 그룹 이름")
                .status(GroupStatus.ACTIVE)
                .build();

        // when & then
        assertThatThrownBy(() -> groupService.updateGroup(memberId, groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_MEMBER_NOT_FOUND.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupId(memberId, groupId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹 수정 시 그룹이 존재하지 않는 경우 예외 발생")
    void updateGroup_ThrowsException_WhenGroupNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        // 그룹장은 맞음
        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)).thenReturn(Optional.of(testGroupMember));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        GroupSummary requestDto = GroupSummary.builder()
                .id(groupId)
                .name("수정된 그룹 이름")
                .status(GroupStatus.ACTIVE)
                .build();

        // when & then
        assertThatThrownBy(() -> groupService.updateGroup(memberId, groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_NOT_FOUND.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹 초대코드 생성 성공 테스트")
    void groupInvite_success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId))
                .thenReturn(Optional.of(testGroupMember));
        when(inviteCodeService.getInviteCodeByGroupId(groupId)).thenReturn("INVITE123");

        // when
        GroupInviteDto result = groupService.groupInvite(memberId, groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getInviteCode()).isEqualTo("INVITE123");
    }

    @Test
    @DisplayName("그룹 초대코드 생성시 그룹 멤버가 존재하지 않으면 예외 발생")
    void groupInvite_groupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 999L;

        when(groupMemberRepository.findByMemberIdAndGroupId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.groupInvite(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GROUP_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("그룹 초대코드 생성시 그룹장이 아닌 경우 예외 발생")
    void groupInvite_notLeader() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        ReflectionTestUtils.setField(testGroupMember, "isLeader", false);

        when(groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId))
                .thenReturn(Optional.of(testGroupMember));

        // when & then
        assertThatThrownBy(() -> groupService.groupInvite(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(NOT_GROUP_LEADER.getMessage());
    }

    @Test
    @DisplayName("그룹 해체 성공 테스트 - 그룹장 1명만 남은 경우")
    void deleteGroup_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE)).thenReturn(1);
        when(groupMemberRepository.findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when
        groupService.deleteGroup(memberId, groupId);

        // then
        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository).countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 그룹 해체 시 예외 발생")
    void deleteGroup_ThrowsException_WhenGroupNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 999L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroup(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GROUP_NOT_FOUND.getMessage());

        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository, never()).countByGroupIdAndStatus(any(), any());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹에 다른 멤버가 남아있을 때 해체 시 예외 발생")
    void deleteGroup_ThrowsException_WhenMembersLeft() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE)).thenReturn(3); // 3명 있음

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroup(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GROUP_MEMBERS_LEFT_IN_GROUP.getMessage());

        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository).countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository, never()).findByGroupIdAndStatusAndIsLeaderTrue(any(), any());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹장이 존재하지 않을 때 예외 발생")
    void deleteGroup_ThrowsException_WhenLeaderNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE)).thenReturn(1);
        when(groupMemberRepository.findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroup(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GROUP_MEMBER_NOT_FOUND.getMessage());

        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository).countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹장이 아닌 사용자가 그룹 해체 시 예외 발생")
    void deleteGroup_ThrowsException_WhenNotLeader() {
        // given
        Long notLeaderMemberId = 999L; // 다른 사용자 ID
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE)).thenReturn(1);
        when(groupMemberRepository.findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroup(notLeaderMemberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(NOT_GROUP_LEADER.getMessage());

        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository).countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹 멤버 목록 조회 성공 테스트")
    void getGroupMembers_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        // List 조회를 위해 다른 사용자를 그룹에 추가
        Member anotherMember = Member.builder()
                .name("다른 사용자")
                .email("test2@example.com")
                .password("password1234")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(anotherMember, "id", 2L);

        GroupMember anotherGroupMember = GroupMember.builder()
                .member(anotherMember)
                .group(testGroup)
                .nickname("다른 사용자")
                .isLeader(false)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(anotherGroupMember, "id", 2L);

        List<GroupMember> groupMembers = List.of(testGroupMember, anotherGroupMember);

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);
        when(groupMemberRepository.findAllByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(groupMembers);

        // when
        List<GroupMemberSummary> result = groupService.getGroupMembers(memberId, groupId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMemberId()).isEqualTo(1L);
        assertThat(result.get(0).getIsLeader()).isTrue();
        assertThat(result.get(1).getMemberId()).isEqualTo(2L);
        assertThat(result.get(1).getIsLeader()).isFalse();

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).findAllByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹 멤버 목록 조회 시 그룹 멤버가 아닐 때 예외 발생 테스트")
    void getGroupMembers_ThrowsException_WhenNotGroupMember() {
        // given
        Long memberId = 999L;
        Long groupId = 1L;

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> groupService.getGroupMembers(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_GROUP_MEMBER.getMessage());

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository, never()).findAllByGroupIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("그룹 멤버 조회 성공 테스트")
    void getGroupMember_success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        Long groupMemberId = 2L;

        Member otherMember = Member.builder()
                .name("다른 멤버")
                .build();
        ReflectionTestUtils.setField(otherMember, "id", 2L);
        GroupMember otherGroupMember = GroupMember.builder()
                .member(otherMember)
                .group(testGroup)
                .nickname("다른 그룹멤버")
                .status(GroupMemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(otherGroupMember, "id", 2L);

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);
        when(groupMemberRepository.findById(groupMemberId))
                .thenReturn(Optional.of(otherGroupMember));

        // when
        GroupMemberSummary result = groupService.getGroupMember(memberId, groupId, groupMemberId);

        // then
        assertThat(result.getId()).isEqualTo(groupMemberId);
        assertThat(result.getMemberId()).isEqualTo(otherMember.getId());
        assertThat(result.getNickname()).isEqualTo("다른 그룹멤버");

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).findById(groupMemberId);
    }

    @Test
    @DisplayName("그룹 멤버 조회시 그룹 멤버가 아닐 때 예외 발생 테스트")
    void getGroupMember_ThrowsException_WhenNotGroupMember() {
        // given
        Long memberId = 1L;
        Long groupId = 2L;
        Long groupMemberId = 2L;

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> groupService.getGroupMember(memberId, groupId, groupMemberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(NOT_GROUP_MEMBER.getMessage());

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("그룹 멤버 조회시 그룹 멤버가 존재하지 않을 때 예외 발생")
    void getGroupMember_ThrowsException_WhenGroupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        Long groupMemberId = 2L;

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);
        when(groupMemberRepository.findById(groupMemberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.getGroupMember(memberId, groupId, groupMemberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GROUP_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("그룹 멤버 조회시 다른 그룹의 멤버일 때 예외 발생")
    void getGroupMember_ThrowsException_WhenGroupMismatch() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        Long groupMemberId = 2L;

        Group anotherGroup = Group.builder()
                .name("다른 그룹")
                .build();
        ReflectionTestUtils.setField(anotherGroup, "id", 2L);
        Member otherMember = Member.builder()
                .name("다른 멤버")
                .build();
        ReflectionTestUtils.setField(otherMember, "id", 2L);
        GroupMember otherGroupMember = GroupMember.builder()
                .member(otherMember)
                .group(anotherGroup) // groupId 불일치
                .status(GroupMemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(otherGroupMember, "id", 2L);

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);
        when(groupMemberRepository.findById(groupMemberId))
                .thenReturn(Optional.of(otherGroupMember));

        // when & then
        assertThatThrownBy(() -> groupService.getGroupMember(memberId, groupId, groupMemberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(NOT_GROUP_MEMBER.getMessage());
    }

    @Test
    @DisplayName("그룹 가입 성공 테스트")
    void joinGroup_Success() {
        // given
        Long memberId = 2L;
        String inviteCode = "INVITE123";
        String nickname = "새로운 멤버";

        Member newMember = Member.builder()
                .name("새로운 사용자")
                .email("newuser@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(newMember, "id", 2L);

        GroupJoinDto groupJoinDto = new GroupJoinDto();
        ReflectionTestUtils.setField(groupJoinDto, "nickname", nickname);
        ReflectionTestUtils.setField(groupJoinDto, "inviteCode", inviteCode);

        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)).thenReturn(false);
        when(inviteCodeService.getGroupIdByInviteCode(inviteCode)).thenReturn(1L);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(newMember));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        GroupMemberSummary result = groupService.joinGroup(memberId, groupJoinDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getGroupId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getIsLeader()).isFalse();
        assertThat(result.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(result.getJoinedAt()).isNotNull();

        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(inviteCodeService).getGroupIdByInviteCode(inviteCode);
        verify(memberRepository).findById(memberId);
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("이미 그룹에 가입한 사용자가 가입 시도시 예외 발생")
    void joinGroup_ThrowsException_WhenAlreadyInGroup() {
        // given
        Long memberId = 1L;
        GroupJoinDto groupJoinDto = new GroupJoinDto();
        ReflectionTestUtils.setField(groupJoinDto, "nickname", "닉네임");
        ReflectionTestUtils.setField(groupJoinDto, "inviteCode", "INVITE123");

        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> groupService.joinGroup(memberId, groupJoinDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ALREADY_IN_GROUP.getMessage());

        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(inviteCodeService, never()).getGroupIdByInviteCode(any());
        verify(memberRepository, never()).findById(any());
        verify(groupRepository, never()).findById(any());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("유효하지 않은 초대코드로 그룹 가입 시도시 예외 발생")
    void joinGroup_ThrowsException_WhenInvalidInviteCode() {
        // given
        Long memberId = 2L;
        String invalidInviteCode = "INVALID_CODE";

        GroupJoinDto groupJoinDto = new GroupJoinDto();
        ReflectionTestUtils.setField(groupJoinDto, "nickname", "닉네임");
        ReflectionTestUtils.setField(groupJoinDto, "inviteCode", invalidInviteCode);

        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)).thenReturn(false);
        when(inviteCodeService.getGroupIdByInviteCode(invalidInviteCode))
                .thenThrow(new CustomException(INVITE_CODE_INVALID));

        // when & then
        assertThatThrownBy(() -> groupService.joinGroup(memberId, groupJoinDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(INVITE_CODE_INVALID.getMessage());

        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(inviteCodeService).getGroupIdByInviteCode(invalidInviteCode);
        verify(memberRepository, never()).findById(any());
        verify(groupRepository, never()).findById(any());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("그룹 멤버 정보 수정 성공 테스트")
    void updateGroupMember_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        String newNickname = "수정된 닉네임";
        GroupMemberSummary requestDto = GroupMemberSummary.builder()
                .id(1L)
                .memberId(memberId)
                .groupId(groupId)
                .nickname(newNickname)
                .build();

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));
        when(groupMemberRepository.save(any(GroupMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        GroupMemberSummary result = groupService.updateGroupMember(memberId, groupId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(newNickname);
        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(groupMemberRepository).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("그룹 멤버 정보 수정시 그룹 멤버가 존재하지 않으면 예외 발생")
    void updateGroupMember_groupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 999L;
        GroupMemberSummary requestDto = GroupMemberSummary.builder()
                .id(1L)
                .memberId(memberId)
                .groupId(groupId)
                .nickname("수정된 닉네임")
                .build();

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.updateGroupMember(memberId, groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("그룹장 이양 성공 테스트")
    void transferLeader_Success() {
        // given
        Long groupId = 1L;
        Long newLeaderMemberId = 2L;

        // 그룹장을 이양받을 멤버
        Member newMember = Member.builder()
                .name("새로운 사용자")
                .email("new@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(newMember, "id", newLeaderMemberId);

        GroupMember newGroupMember = GroupMember.builder()
                .member(newMember)
                .nickname("새로운 사용자")
                .isLeader(false)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .group(testGroup)
                .build();
        ReflectionTestUtils.setField(newGroupMember, "id", 2L);

        LeaderTransferRequestDto requestDto = new LeaderTransferRequestDto();
        ReflectionTestUtils.setField(requestDto, "newLeaderId", 2L);

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(testMember.getId(), groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));
        when(groupMemberRepository.findById(newGroupMember.getId()))
                .thenReturn(Optional.of(newGroupMember));

        // when
        LeaderTransferResponseDto response = groupService.transferLeader(testMember.getId(), groupId, requestDto);

        // then
        assertThat(testGroupMember.getIsLeader()).isFalse();
        assertThat(newGroupMember.getIsLeader()).isTrue();
        assertThat(response.getPreviousLeaderId()).isEqualTo(testGroupMember.getId());
        assertThat(response.getNewLeaderId()).isEqualTo(newGroupMember.getId());
    }

    @Test
    @DisplayName("요청자가 그룹장이 아니면 예외 발생 테스트")
    void transferLeader_Fails_WhenRequesterIsNotLeader() {
        // given
        Long groupId = 1L;

        // 요청자가 그룹장이 아닌 경우
        ReflectionTestUtils.setField(testGroupMember, "isLeader", false);

        LeaderTransferRequestDto requestDto = new LeaderTransferRequestDto();
        ReflectionTestUtils.setField(requestDto, "newLeaderId", 999L);


        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(testMember.getId(), groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when & then
        assertThatThrownBy(() -> groupService.transferLeader(testMember.getId(), groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_GROUP_LEADER.getMessage());
    }

    @Test
    @DisplayName("새로운 리더가 같은 그룹이 아니면 예외 발생 테스트")
    void transferLeader_Fails_WhenNewLeaderNotInSameGroup() {
        // given
        Long groupId = 1L;
        Long prevLeaderMemberId = 1L;

        // 다른 그룹에 속하는 멤버
        Group otherGroup = Group.builder()
                .name("다른 그룹")
                .status(GroupStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(otherGroup, "id", 99L);

        Member newMember = Member.builder()
                .name("다른 그룹 사용자")
                .email("other@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(newMember, "id", 2L);

        GroupMember newGroupMember = GroupMember.builder()
                .member(newMember)
                .nickname("다른 그룹 사용자")
                .isLeader(false)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .group(otherGroup)
                .build();
        ReflectionTestUtils.setField(newGroupMember, "id", 2L);

        LeaderTransferRequestDto requestDto = new LeaderTransferRequestDto();
        ReflectionTestUtils.setField(requestDto, "newLeaderId", 2L);

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(prevLeaderMemberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));
        when(groupMemberRepository.findById(newGroupMember.getId()))
                .thenReturn(Optional.of(newGroupMember));

        // when & then
        assertThatThrownBy(() -> groupService.transferLeader(testMember.getId(), groupId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_GROUP_MEMBER.getMessage());
    }

    @Test
    @DisplayName("그룹장 여부 조회 성공 - 그룹장인 경우")
    void getIsLeader_Success_WithIsLeaderTrue() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when
        IsLeaderDto result = groupService.getIsLeader(memberId, groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLeader()).isEqualTo(true);

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹장 여부 조회 성공 - 그룹장이 아닌 경우")
    void getIsLeader_Success_WithIsLeaderFalse() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        ReflectionTestUtils.setField(testGroupMember, "isLeader", false);

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when
        IsLeaderDto result = groupService.getIsLeader(memberId, groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLeader()).isEqualTo(false);

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹장 여부 조회 시 그룹 멤버가 존재하지 않으면 예외 발생")
    void getIsLeader_ThrowsException_WhenGroupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 999L;

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.getIsLeader(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_MEMBER_NOT_FOUND.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
    }
}
