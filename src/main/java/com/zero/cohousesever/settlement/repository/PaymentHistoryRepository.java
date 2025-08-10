package com.zero.cohousesever.settlement.repository;

import com.zero.cohousesever.settlement.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
