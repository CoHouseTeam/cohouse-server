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
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupSummary createGroup(Long memberId, GroupNameDto groupNameDto) {

        Member member = memberRepository.findById(memberId).orElseThrow();

        Group group = groupRepository.save(
                Group.builder()
                        .name(groupNameDto.getGroupName())
                        .status(GroupStatus.ACTIVE)
                        .build()
        );

        GroupMember leader = groupMemberRepository.save(
                GroupMember.builder()
                        .member(member)
                        .group(group)
                        .nickname(member.getName()) // 이름을 기본 닉네임으로 사용
                        .isLeader(true)
                        .status(GroupMemberStatus.ACTIVE)
                        .joinedAt(LocalDateTime.now())
                        .build()
        );

        group.addMember(leader);

        groupRepository.save(group);
        groupMemberRepository.save(leader);

        return GroupSummary.fromEntity(group);
    }
}
