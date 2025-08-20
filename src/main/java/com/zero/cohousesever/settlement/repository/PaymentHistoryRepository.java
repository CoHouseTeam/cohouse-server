package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findBySenderAndTransferDateBetween(Member sender, LocalDateTime fromDate, LocalDateTime toDate);

    List<PaymentHistory> findBySenderAndSettlementIdAndTransferDateBetween(Member sender, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate);

    @Query("select p from PaymentHistory p where p.sender = :sender and p.settlement.group.id = :groupId and p.transferDate between :fromDate and :toDate")
    List<PaymentHistory> findBySenderAndGroupIdAndTransferDateBetween(Member sender, Long groupId, LocalDateTime fromDate, LocalDateTime toDate);

    @Query("select p from PaymentHistory p where p.sender = :sender and p.settlement.group.id = :groupId and p.settlement.id = :settlementId and p.transferDate between :fromDate and :toDate")
    List<PaymentHistory> findBySenderAndGroupIdAndSettlementIdAndTransferDateBetween(Member sender, Long groupId, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate);
}