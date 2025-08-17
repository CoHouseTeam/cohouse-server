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

        Member member = memberRepository.findById(memberId).orElseThrow(); // TODO: 적절한 예외 던지기

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

        Group group = groupRepository.findById(groupId).orElseThrow(); // TODO: 적절한 예외 던지기

        return GroupSummary.fromEntity(group);
    }
}
