package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementDto;
import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.entity.Participant;
import com.zero.cohousesever.settlement.entity.PaymentStatus;
import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import com.zero.cohousesever.settlement.repository.ParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;

    /**
     * 정산 등록
     */
    public SettlementDto createSettlement(CreateSettlementRequest request) {
        Settlement settlement = new Settlement();
        settlement.setTitle(request.getTitle());
        settlement.setDescription(request.getDescription());
        settlement.setCategory(request.getCategory());

        settlement.setSettlementAmount(request.getSettlementAmount());
        settlement.setStatus(SettlementStatus.PENDING);

        List<Participant> participants = new ArrayList<>();
        for (Long memberId : request.getParticipantIds()) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

            Participant participant = new Participant();
            participant.setMember(member);
            participant.setSettlement(settlement);
            participant.setStatus(PaymentStatus.PENDING);
            participant.setShareAmount(calculateShareAmount(request.getSettlementAmount(), request.getParticipantIds().size()));
            participants.add(participant);
        }
        settlement.setParticipants(participants);

        Settlement savedSettlement = settlementRepository.save(settlement);
        return SettlementDto.fromEntity(savedSettlement);
    }

    // 배분 금액 계산 함수
    public BigDecimal calculateShareAmount(BigDecimal totalAmount, int participantCount) {
        if (participantCount <= 0) {
            throw new IllegalArgumentException("참여자 수는 1 이상이어야 합니다.");
        }
        return totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 정산 목록 조회 (페이징 및 필터링 포함)
     */
    public void getSettlements() {
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
