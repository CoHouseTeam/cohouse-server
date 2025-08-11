package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.repository.ParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
    public void getSettlements() {
    }

    /**
     * 신규 정산 등록
     */
    public void createSettlement() {
    }

    /**
     * 특정 정산 상세 정보 조회
     */
    public void getSettlement() {
    }

    /**
     * 정산 삭제 또는 취소 처리
     */
    public void deleteSettlement() {
    }

    /**
     * 정산 참여자 목록 조회
     */
    public void getParticipants() {
    }

    /**
     * 정산 참여자 추가
     */
    public void addParticipant() {
    }

    /**
     * 정산 참여자 제거
     */
    public void removeParticipant() {
    }

    /**
     * 영수증 이미지 업로드 및 처리
     */
    public void uploadReceiptImage() {
    }

    /**
     * 정산 전체 히스토리 조회
     */
    public List<SettlementHistoryResponse> getSettlementHistories(Long groupId, Long settlementId) {
        return null;
    }

    /**
     * 그룹의 정산 히스토리 조회
     */
    public List<SettlementHistoryResponse> getGroupSettlementHistories(Long groupId, Long settlementId, LocalDate fromDate, LocalDate toDate) {
        return null;
    }
}
