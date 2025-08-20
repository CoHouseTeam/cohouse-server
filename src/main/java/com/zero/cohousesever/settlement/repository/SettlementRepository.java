package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 회원이 참여한 모든 정산 조회
    @Query("SELECT DISTINCT s FROM Settlement s " +
            "JOIN s.settlementParticipants sp " +
            "WHERE sp.member = :member " +
            "ORDER BY s.createdAt DESC")
    List<Settlement> findAllByParticipantMember(Member member);

    List<Settlement> findAllByGroupOrderByCreatedAtDesc(Group group);
}
