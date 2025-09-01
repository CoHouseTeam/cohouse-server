package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestReasonDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestSummary;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.enums.GroupStatus;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import com.zero.cohousesever.group.repository.GroupLeaveRequestRepository;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
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
class GroupLeaveServiceTest {

    @Mock
    private GroupLeaveRequestRepository groupLeaveRequestRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private GroupLeaveService groupLeaveService;

    private Member testMember;
    private Group testGroup;
    private GroupMember testGroupMember;
    private LeaveRequestReasonDto leaveRequestReasonDto;
    private GroupLeaveRequest testGroupLeaveRequest;

    @BeforeEach
    void setUp() {
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
                .nickname("테스트 닉네임")
                .isLeader(false)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testGroupMember, "id", 1L);

        leaveRequestReasonDto = new LeaveRequestReasonDto();
        ReflectionTestUtils.setField(leaveRequestReasonDto, "reason", "계약 만료");

        testGroupLeaveRequest = GroupLeaveRequest.builder()
                .member(testMember)
                .group(testGroup)
                .reason("계약 만료")
                .status(LeaveRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testGroupLeaveRequest, "id", 1L);
    }

    @Test
    @DisplayName("그룹 탈퇴 요청 성공 테스트")
    void requestGroupLeave_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));
        when(settlementRepository.existsByIdAndStatus(memberId, SettlementStatus.PENDING))
                .thenReturn(false);
        when(groupLeaveRequestRepository.save(any(GroupLeaveRequest.class)))
                .thenReturn(testGroupLeaveRequest);

        // when
        LeaveRequestSummary result = groupLeaveService.requestGroupLeave(memberId, groupId, leaveRequestReasonDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getReason()).isEqualTo("계약 만료");
        assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.PENDING);

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(settlementRepository).existsByIdAndStatus(memberId, SettlementStatus.PENDING);
        verify(groupLeaveRequestRepository).save(any(GroupLeaveRequest.class));
    }

    @Test
    @DisplayName("그룹 멤버가 존재하지 않을 때 예외 발생 테스트")
    void requestGroupLeave_ThrowsException_WhenGroupMemberNotFound() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupLeaveService.requestGroupLeave(memberId, groupId, leaveRequestReasonDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_MEMBER_NOT_FOUND.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(settlementRepository, never()).existsByIdAndStatus(any(), any());
        verify(groupLeaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("미결제된 정산이 있을 때 예외 발생 테스트")
    void requestGroupLeave_ThrowsException_WhenUnsettledSettlementExists() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));
        when(settlementRepository.existsByIdAndStatus(memberId, SettlementStatus.PENDING))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> groupLeaveService.requestGroupLeave(memberId, groupId, leaveRequestReasonDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNSETTLED_SETTLEMENT_EXISTS.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(settlementRepository).existsByIdAndStatus(memberId, SettlementStatus.PENDING);
        verify(groupLeaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("그룹 리더가 탈퇴 요청할 때 예외 발생 테스트")
    void requestGroupLeave_ThrowsException_WhenLeaderRequests() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        ReflectionTestUtils.setField(testGroupMember, "isLeader", true);

        when(groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(testGroupMember));

        // when & then
        assertThatThrownBy(() -> groupLeaveService.requestGroupLeave(memberId, groupId, leaveRequestReasonDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(GROUP_LEADER_LEAVE_FORBIDDEN.getMessage());

        verify(groupMemberRepository).findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE);
        verify(settlementRepository, never()).existsByIdAndStatus(any(), any());
        verify(groupLeaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("그룹 탈퇴 요청 목록 조회 성공 테스트")
    void getGroupLeaveList_Success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        ReflectionTestUtils.setField(testGroupMember, "isLeader", true);

        // 추가 멤버 생성
        Member member2 = Member.builder()
                .name("테스트 사용자2")
                .email("test2@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(member2, "id", 2L);

        Member member3 = Member.builder()
                .name("테스트 사용자3")
                .email("test3@example.com")
                .password("password123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(member3, "id", 3L);

        // 탈퇴 요청 목록 생성
        GroupLeaveRequest request1 = GroupLeaveRequest.builder()
                .member(member2)
                .group(testGroup)
                .reason("개인 사정")
                .status(LeaveRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(request1, "id", 1L);

        GroupLeaveRequest request2 = GroupLeaveRequest.builder()
                .member(member3)
                .group(testGroup)
                .reason("이사로 인한 탈퇴")
                .status(LeaveRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(request2, "id", 2L);

        List<GroupLeaveRequest> leaveRequests = List.of(request1, request2);

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId))
                .thenReturn(true);
        when(groupLeaveRequestRepository.findByGroupIdAndStatus(groupId, LeaveRequestStatus.PENDING))
                .thenReturn(leaveRequests);

        // when
        List<LeaveRequestSummary> result = groupLeaveService.getGroupLeaveList(memberId, groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMemberId()).isEqualTo(2L);
        assertThat(result.get(0).getReason()).isEqualTo("개인 사정");
        assertThat(result.get(0).getStatus()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(result.get(1).getMemberId()).isEqualTo(3L);
        assertThat(result.get(1).getReason()).isEqualTo("이사로 인한 탈퇴");
        assertThat(result.get(1).getStatus()).isEqualTo(LeaveRequestStatus.PENDING);

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId);
        verify(groupLeaveRequestRepository).findByGroupIdAndStatus(groupId, LeaveRequestStatus.PENDING);
    }

    @Test
    @DisplayName("탈퇴 요청 목록 조회 시 그룹장이 아닌 경우 예외 발생 테스트")
    void getGroupLeaveList_ThrowsException_WhenNotLeader() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;

        when(groupMemberRepository.existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> groupLeaveService.getGroupLeaveList(memberId, groupId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_GROUP_LEADER.getMessage());

        verify(groupMemberRepository).existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId);
        verify(groupLeaveRequestRepository, never()).findByGroupIdAndStatus(any(), any());
    }
}