package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.settlement.dto.PaymentDto;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final ParticipantRepository participantRepository;

    public SettlementService(SettlementRepository settlementRepository,
                             ParticipantRepository participantRepository) {
        this.settlementRepository = settlementRepository;
        this.participantRepository = participantRepository;
    }

    // TODO: 실제 서비스 로직 구현 예정

    /**
     * 페이먼트 목록 조회 (페이징 및 필터링 포함)
     */
    public Page<PaymentDto> getPayments(String status, int page, int size, String sort) {
        return Page.empty();
    }

    /**
     * 특정 payment 상세 조회
     */
    public PaymentDto getPayment(Long paymentId) {
        return null;
    }

    /**
     * 새로운 payment 등록
     */
    public PaymentDto createPayment(CreatePaymentRequest request) {
        return null;
    }

    /**
     * 기존 payment 정보 수정
     */
    public void updatePayment(Long paymentId, UpdatePaymentRequest request) {

    }

    /**
     * payment 삭제 또는 취소 처리
     */
    public void deletePayment(Long paymentId) {
    }
}

