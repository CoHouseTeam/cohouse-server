package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.settlement.SettlementHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {
    // 내가 참여한 정산들의 히스토리 조회
    @Query("SELECT sh FROM SettlementHistory sh " +
            "WHERE EXISTS (SELECT 1 FROM SettlementParticipant sp " +
            "WHERE sp.settlement = sh.settlement AND sp.member = :member) ")
    Page<SettlementHistory> findAllBySender(Member member, Pageable pageable);
}
