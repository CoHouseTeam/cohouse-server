package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.group.dto.group.GroupInviteDto;
import com.zero.cohousesever.group.dto.group.GroupNameDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
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

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final InviteCodeService inviteCodeService;

    @Transactional
    public GroupSummary createGroup(Long memberId, GroupNameDto groupNameDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        GroupMember leader = GroupMember.builder()
                .member(member)
                .nickname(member.getName()) // 이름을 기본 닉네임으로 사용
                .isLeader(true)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        Group group = Group.builder()
                .name(groupNameDto.getGroupName())
                .status(GroupStatus.ACTIVE)
                .build();


        group.addMember(leader);

        // Group만 저장하면 cascade로 GroupMember도 함께 저장됨
        groupRepository.save(group);

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
}
