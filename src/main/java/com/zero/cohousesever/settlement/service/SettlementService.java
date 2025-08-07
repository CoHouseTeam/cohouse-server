package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementDto;
import com.zero.cohousesever.settlement.repository.ParticipantRepository;
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
     * 정산 목록 조회 (페이징 및 필터링 포함)
     */
    public Page<SettlementDto> getSettlements(String status, int page, int size, String sort) {
        return Page.empty();
    }

    /**
     * 특정 정산 상세 조회
     */
    public SettlementDto getSettlement(Long settlementId) {
        return null;
    }

    /**
     * 새로운 정산 등록
     */
    public SettlementDto createSettlement(CreateSettlementRequest request) {
        return null;
    }

    /**
     * 정산 취소 처리
     */
    public void cancelSettlement(Long settlementId) {
    }
}
