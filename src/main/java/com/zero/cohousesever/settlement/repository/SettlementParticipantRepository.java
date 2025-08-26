package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.entity.SettlementParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementParticipantRepository extends JpaRepository<SettlementParticipant, Long> {
    List<SettlementParticipant> findAllBySettlement(Settlement settlement);

    boolean existsBySettlementIdAndMember(Long settlementId, Member member);
}
