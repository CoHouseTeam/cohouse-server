package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestReasonDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestStatusDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestSummary;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import com.zero.cohousesever.group.repository.GroupLeaveRequestRepository;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class GroupLeaveService {

    private final GroupRepository groupRepository;
    private final GroupLeaveRequestRepository groupLeaveRequestRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;

    public LeaveRequestSummary requestGroupLeave(Long memberId, Long groupId, LeaveRequestReasonDto requestDto) {

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        if (groupMember.getIsLeader()) {
            throw new CustomException(GROUP_LEADER_LEAVE_FORBIDDEN);
        }

        if (settlementRepository.existsByIdAndStatus(memberId, SettlementStatus.PENDING)) {
            throw new CustomException(UNSETTLED_SETTLEMENT_EXISTS);
        }

        GroupLeaveRequest groupLeaveRequest = GroupLeaveRequest.builder()
                .member(groupMember.getMember())
                .group(groupMember.getGroup())
                .reason(requestDto.getReason())
                .status(LeaveRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        GroupLeaveRequest saved = groupLeaveRequestRepository.save(groupLeaveRequest);

        return LeaveRequestSummary.fromEntity(saved);
    }

    public List<LeaveRequestSummary> getGroupLeaveList(Long memberId, Long groupId) {
        if (!groupMemberRepository.existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId)) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        List<GroupLeaveRequest> list = groupLeaveRequestRepository.findByGroupIdAndStatus(groupId, LeaveRequestStatus.PENDING);

        return list.stream()
                .map(LeaveRequestSummary::fromEntity)
                .toList();
    }

    public LeaveRequestSummary respondGroupLeave(Long memberId, Long groupId, Long leaveRequestId, LeaveRequestStatusDto requestDto) {
        if (!groupMemberRepository.existsByMemberIdAndGroupIdAndIsLeaderTrue(memberId, groupId)) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        GroupLeaveRequest groupLeaveRequest = groupLeaveRequestRepository.findById(leaveRequestId)
                .orElseThrow();

        Member member = groupLeaveRequest.getMember();
        Group group = groupLeaveRequest.getGroup();
        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(member.getId(), group.getId(), GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_ALREADY_INACTIVE));

        LeaveRequestStatus status = requestDto.getStatus();
        if (status.equals(LeaveRequestStatus.ACCEPTED)) {
            // 남은 정산이 있는지 탈퇴 전에 재검사
            if (settlementRepository.existsByIdAndStatus(memberId, SettlementStatus.PENDING)) {
                throw new CustomException(UNSETTLED_SETTLEMENT_EXISTS);
            }

            group.removeMember(groupMember);
            groupRepository.save(group);
            groupMemberRepository.save(groupMember);
        }

        groupLeaveRequest.respond(status);
        GroupLeaveRequest saved = groupLeaveRequestRepository.save(groupLeaveRequest);

        return LeaveRequestSummary.fromEntity(saved);
    }
}
