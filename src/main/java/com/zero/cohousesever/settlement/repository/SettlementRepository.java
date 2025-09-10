package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.settlement.Settlement;
import com.zero.cohousesever.settlement.entity.settlement.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 회원이 참여한 모든 정산 조회
    @Query("SELECT DISTINCT s FROM Settlement s " +
            "JOIN s.settlementParticipants sp " +
            "WHERE sp.member = :member ")
    Page<Settlement> findAllByParticipantMember(@Param("member") Member member, Pageable pageable);

    Page<Settlement> findAllByGroup(Group group, Pageable pageable);

    boolean existsByIdAndStatus(Long memberId, SettlementStatus settlementStatus);
}
