package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestReasonDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestSummary;
import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import com.zero.cohousesever.group.repository.GroupLeaveRequestRepository;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupLeaveService {

    private final GroupLeaveRequestRepository groupLeaveRequestRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;

    public LeaveRequestSummary requestGroupLeave(Long memberId, Long groupId, LeaveRequestReasonDto requestDto) {

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND));

        if (settlementRepository.existsByIdAndStatus(memberId, SettlementStatus.PENDING)) {
            throw new CustomException(ErrorCode.UNSETTLED_SETTLEMENT_EXISTS);
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
}
