package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByMemberIdAndGroupId(Long memberId, Long groupId);

    Optional<GroupMember> findByMemberIdAndStatus(Long memberId, GroupMemberStatus status);

    Optional<GroupMember> findByMemberIdAndGroupIdAndStatus(Long memberId, Long groupId, GroupMemberStatus status);

    List<GroupMember> findAllByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    Boolean existsByMemberIdAndGroupIdAndStatus(Long memberId, Long groupId, GroupMemberStatus status);

    // 그룹장 여부 확인
    boolean existsByGroupAndMemberAndIsLeaderTrue(Group group, Member member);

}
