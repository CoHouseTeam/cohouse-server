package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.group.dto.group.GroupInviteDto;
import com.zero.cohousesever.group.dto.group.GroupJoinDto;
import com.zero.cohousesever.group.dto.group.GroupCreateDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
import com.zero.cohousesever.group.dto.groupmember.GroupMemberSummary;
import com.zero.cohousesever.group.dto.groupmember.IsLeaderDto;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferRequestDto;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferResponseDto;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.enums.GroupStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final InviteCodeService inviteCodeService;

    @Transactional
    public GroupSummary createGroup(Long memberId, GroupCreateDto groupCreateDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        if (groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)) {
            throw new CustomException(ALREADY_IN_GROUP);
        }

        GroupMember leader = GroupMember.builder()
                .member(member)
                .nickname(groupCreateDto.getLeaderNickname())
                .isLeader(true)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        Group group = Group.builder()
                .name(groupCreateDto.getGroupName())
                .status(GroupStatus.ACTIVE)
                .build();

        group.addMember(leader);

        // Group만 저장하면 cascade로 GroupMember도 함께 저장됨
        groupRepository.save(group);

        return GroupSummary.fromEntity(group);
    }

    public GroupSummary getGroupByMemberId(Long memberId) {

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        Group group = groupMember.getGroup();

        return GroupSummary.fromEntity(group);
    }

    public GroupSummary getGroup(Long groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));

        return GroupSummary.fromEntity(group);
    }

    public GroupSummary updateGroup(Long memberId, Long groupId, GroupSummary requestDto) {

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        // 그룹장이 아닌 경우 그룹 정보 수정 불가
        if (!groupMember.getIsLeader()) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));

        group.updateName(requestDto.getName());

        Group saved = groupRepository.save(group);

        return GroupSummary.fromEntity(saved);
    }

    @Transactional
    public void deleteGroup(Long memberId, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));

        if (groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE) > 1) {
            throw new CustomException(GROUP_MEMBERS_LEFT_IN_GROUP);
        }

        GroupMember leader = groupMemberRepository.findByGroupIdAndStatusAndIsLeaderTrue(groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        if (!Objects.equals(memberId, leader.getMember().getId())) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        group.removeMember(leader);
        group.updateStatus(GroupStatus.INACTIVE);
    }

    public GroupInviteDto groupInvite(Long memberId, Long groupId) {

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        if (!groupMember.getIsLeader()) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        String inviteCode = inviteCodeService.generateInviteCode(groupId);

        return GroupInviteDto.builder()
                .groupId(groupId)
                .inviteCode(inviteCode)
                .build();
    }

    public List<GroupMemberSummary> getGroupMembers(Long memberId, Long groupId) {

        if (!groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)) {
            throw new CustomException(NOT_GROUP_MEMBER);
        }

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);

        return groupMembers.stream().map(GroupMemberSummary::fromEntity).toList();
    }

    public GroupMemberSummary getGroupMember(Long memberId, Long groupId, Long groupMemberId) {

        // 본인 그룹만 조회 가능
        if (!groupMemberRepository.existsByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)) {
            throw new CustomException(NOT_GROUP_MEMBER);
        }

        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));
        if (!Objects.equals(groupMember.getGroup().getId(), groupId)) {
            throw new CustomException(NOT_GROUP_MEMBER);
        }

        return GroupMemberSummary.fromEntity(groupMember);
    }

    public GroupMemberSummary updateGroupMember(Long memberId, Long groupId, GroupMemberSummary requestDto) {
        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        // 그룹멤버 정보 수정
        groupMember.updateNickname(requestDto.getNickname());

        return GroupMemberSummary.fromEntity(groupMemberRepository.save(groupMember));
    }

    @Transactional
    public GroupMemberSummary joinGroup(Long memberId, GroupJoinDto requestDto) {

        if (groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)) {
            throw new CustomException(ALREADY_IN_GROUP);
        }

        String code = requestDto.getInviteCode();

        Long groupId = inviteCodeService.validateInviteCode(code);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));
        GroupMember groupMember = GroupMember.builder()
                .member(member)
                .group(group)
                .nickname(requestDto.getNickname())
                .isLeader(false)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        group.addMember(groupMember);
        groupRepository.save(group); // cascade 설정에 의해 groupMember도 자동 저장

        return GroupMemberSummary.fromEntity(groupMember);
    }

    @Transactional
    public LeaderTransferResponseDto transferLeader(Long memberId, Long groupId, LeaderTransferRequestDto requestDto) {

        // 요청자가 그룹장인지 확인
        GroupMember prevLeader = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));
        if (!prevLeader.getIsLeader()) {
            throw new CustomException(NOT_GROUP_LEADER);
        }

        // 이양 받을 멤버가 같은 그룹인지 확인
        GroupMember newLeader = groupMemberRepository.findById(requestDto.getNewLeaderId())
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));
        if (!Objects.equals(newLeader.getGroup().getId(), groupId)) {
            throw new CustomException(NOT_GROUP_MEMBER);
        }

        prevLeader.transferLeader(newLeader);

        return LeaderTransferResponseDto.builder()
                .previousLeaderId(prevLeader.getId())
                .newLeaderId(newLeader.getId())
                .build();
    }

    public IsLeaderDto getIsLeader(Long memberId, Long groupId) {
        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupIdAndStatus(memberId, groupId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(GROUP_MEMBER_NOT_FOUND));

        return IsLeaderDto.builder()
                .isLeader(groupMember.getIsLeader())
                .build();
    }
}
