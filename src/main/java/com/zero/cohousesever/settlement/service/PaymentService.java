package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.settlement.entity.Participant;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import com.zero.cohousesever.settlement.repository.ParticipantRepository;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ParticipantRepository participantRepository;

    /**
     * TODO 참여자가 송금 버튼을 눌러 송금 처리
     */
    public PaymentHistory processPayment(Long participantId, Participant payee, Long amount) {
        // 1. 송금 API 호출
        // 2. 송금 거래 내역 생성 및 저장
        // 3. 참여자의 송금 상태 업데이트 및 저장
        // 4. 저장된 거래 이력 반환
        return null;
    }
}
