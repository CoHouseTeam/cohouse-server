package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.settlement.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
}
