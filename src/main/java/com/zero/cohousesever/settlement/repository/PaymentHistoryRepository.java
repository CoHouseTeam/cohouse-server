package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    Optional<PaymentHistory> findBySenderAndSettlementId(Member member, Long settlementId);
}