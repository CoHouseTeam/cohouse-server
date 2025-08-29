package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByMemberIdAndGroupId(Long memberId, Long groupId);

    Optional<GroupMember> findByMemberIdAndStatus(Long memberId, GroupMemberStatus status);

    Optional<GroupMember> findByMemberIdAndGroupIdAndStatus(Long memberId, Long groupId, GroupMemberStatus status);

//    List<GroupMember> findAllByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, GroupMemberStatus status);

    boolean existsByMemberIdAndGroupIdAndStatus(Long memberId, Long groupId, GroupMemberStatus status);

    // 그룹장 여부 확인
    boolean existsByGroupAndMemberAndIsLeaderTrue(Group group, Member member);

    // 그룹원 여부 확인
    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    // 할 일 스케줄러용:그룹 멤버 조회
    @Query("""
           select gm.member.id
             from GroupMember gm
            where gm.group.id = :groupId
              and gm.status = :status
           """)
    List<Long> findMemberIdsByGroupIdAndStatus(@Param("groupId") Long groupId,
        @Param("status") GroupMemberStatus status);

    // 특정 그룹 내에서 특정 상태(ACTIVE 등)인 그룹멤버 목록 조회
    List<GroupMember> findAllByGroupIdAndStatus(Long groupId, GroupMemberStatus status);
}