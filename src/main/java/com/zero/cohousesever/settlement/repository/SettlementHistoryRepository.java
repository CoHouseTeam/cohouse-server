package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.settlement.entity.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {
}
