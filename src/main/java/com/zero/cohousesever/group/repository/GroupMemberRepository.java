package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByMemberIdAndStatus(Long memberId, GroupMemberStatus status);
}
