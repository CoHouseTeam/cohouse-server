package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    Page<PaymentHistory> findBySender(Member sender, Pageable pageable);

    Page<PaymentHistory> findBySenderAndTransferDateBetween(Member sender, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Page<PaymentHistory> findBySenderAndSettlementIdAndTransferDateBetween(Member sender, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query("select p from PaymentHistory p where p.sender = :sender and p.settlement.group.id = :groupId and p.transferDate between :fromDate and :toDate")
    Page<PaymentHistory> findBySenderAndGroupIdAndTransferDateBetween(Member sender, Long groupId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query("select p from PaymentHistory p where p.sender = :sender and p.settlement.group.id = :groupId and p.settlement.id = :settlementId and p.transferDate between :fromDate and :toDate")
    Page<PaymentHistory> findBySenderAndGroupIdAndSettlementIdAndTransferDateBetween(Member sender, Long groupId, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}