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

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;

    /**
     * 정산 등록
     */
    public SettlementDto createSettlement(Long payerId, CreateSettlementRequest request) {
        Member payer = memberRepository.findById(payerId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + payerId));

        Settlement settlement = new Settlement();
        settlement.setTitle(request.getTitle());
        settlement.setDescription(request.getDescription());
        settlement.setCategory(request.getCategory());

        settlement.setSettlementAmount(request.getSettlementAmount());
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setPayer(payer);

        Set<Long> allParticipantIds = new HashSet<>(request.getParticipantIds());
        allParticipantIds.add(payerId); // 결제자 ID도 포함시킴

        if (request.isEqualDistribution()) {
            // 솜금 금액 균등 분배 시
            Long shareAmount = calculateShareAmount(request.getSettlementAmount(), allParticipantIds.size());
            Long roundingAmount = request.getSettlementAmount() % allParticipantIds.size();
            settlement.setPlatformSupportAmount(roundingAmount);

            List<Participant> participants = new ArrayList<>();
            for (Long memberId : allParticipantIds) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

                Participant participant = new Participant();
                participant.setMember(member);
                participant.setSettlement(settlement);

                if (memberId.equals(payerId)) {
                    participant.setStatus(PaymentStatus.PAID); // 결제자: 송금 완료 상태
                } else {
                    participant.setStatus(PaymentStatus.PENDING); // 참여자: 대기 상태
                }
                participant.setShareAmount(shareAmount);
                participants.add(participant);
            }
            settlement.setParticipants(participants);

        } else {
//            // 송금 금액 직접 분배 시
//            Long manualShares = request.getManualSharedAmount();
//            Long sumShares = manualShares.values().stream().mapToLong(Long::longValue).sum();
//            if (!sumShares.equals(request.getSettlementAmount())) {
//                throw new IllegalArgumentException("참여자별 분배 금액 합계가 총 정산 금액과 일치하지 않습니다.");
//            }
//
//            settlement.setPlatformSupportAmount(0L); // 오차 없음
//
//            for (Long memberId : allParticipantIds) {
//                participant.setShareAmount(manualShares.get(memberId));
//            }
        }
        Settlement savedSettlement = settlementRepository.save(settlement);

        return SettlementDto.fromEntity(savedSettlement);
    }

    // 배분 금액 계산 함수
    public Long calculateShareAmount(Long totalAmount, int participantCount) {
        if (participantCount <= 0) {
            throw new IllegalArgumentException("참여자 수는 1 이상이어야 합니다.");
        }
        return totalAmount / participantCount;
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
