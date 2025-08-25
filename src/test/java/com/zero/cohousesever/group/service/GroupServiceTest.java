package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.dto.group.GroupInviteDto;
import com.zero.cohousesever.group.dto.group.GroupNameDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
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
                .group(testGroup)
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
        assertThatThrownBy(() -> groupService.createGroup(memberId, groupNameDto))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다.");

        verify(memberRepository).findById(memberId);
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
    @DisplayName("활성 그룹 멤버가 없을 때 예외 발생 테스트")
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
                .hasMessage("해당 그룹을 찾을 수 없습니다.");

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
                .hasMessage("그룹장만 접근할 수 있습니다.");

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
                .hasMessage("해당 그룹 멤버를 찾을 수 없습니다."); // GROUP_MEMBER_NOT_FOUND 메시지로 교체

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
                .hasMessage("해당 그룹을 찾을 수 없습니다."); // GROUP_NOT_FOUND 메시지

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
        when(inviteCodeService.generateInviteCode(groupId)).thenReturn("INVITE123");

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

        // expect
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

        // expect
        assertThatThrownBy(() -> groupService.groupInvite(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(NOT_GROUP_LEADER.getMessage());
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
}
