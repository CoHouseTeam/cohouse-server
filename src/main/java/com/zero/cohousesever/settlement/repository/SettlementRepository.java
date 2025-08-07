package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
