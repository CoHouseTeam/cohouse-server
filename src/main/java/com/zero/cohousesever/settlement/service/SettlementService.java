package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementResponseDto;
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
    public SettlementResponseDto createSettlement(Long payerId, CreateSettlementRequest request) {
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
        allParticipantIds.add(payerId); // 결제자 포함

        List<Participant> participants;
        if (request.isEqualDistribution()) {
            participants = createEqualDistributionParticipants(settlement, allParticipantIds, request.getSettlementAmount());
        } else {
//            participants = createManualDistributionParticipants(settlement, allParticipantIds, request.getManualShares(), request.getSettlementAmount());
        }

        settlement.setParticipants(participants);
        Settlement savedSettlement = settlementRepository.save(settlement);

        return SettlementResponseDto.fromEntity(savedSettlement);
    }

    // 균등 분배 참여자 생성 메서드
    private List<Participant> createEqualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Long totalAmount) {
        Long shareAmount = calculateShareAmount(totalAmount, participantIds.size());
        Long remainder = totalAmount % participantIds.size();
        settlement.setPlatformSupportAmount(remainder);

        List<Participant> participants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

            Participant participant = new Participant();
            participant.setMember(member);
            participant.setSettlement(settlement);
            participant.setStatus(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING);
            participant.setShareAmount(shareAmount);
            participants.add(participant);
        }
        return participants;
    }

    // 수동 분배 참여자 생성 메서드
    private List<Participant> createManualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Map<Long, Long> manualShares, Long totalAmount) {
        if (manualShares == null || manualShares.isEmpty()) {
            throw new IllegalArgumentException("Manual shares must be provided for manual distribution.");
        }

        Long sumShares = manualShares.values().stream().mapToLong(Long::longValue).sum();
        if (!sumShares.equals(totalAmount)) {
            throw new IllegalArgumentException("Sum of manual shares does not match total amount.");
        }

        settlement.setPlatformSupportAmount(0L); // 오차 없음

        List<Participant> participants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

            Participant participant = new Participant();
            participant.setMember(member);
            participant.setSettlement(settlement);
            participant.setStatus(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING);
            participant.setShareAmount(manualShares.getOrDefault(memberId, 0L));
            participants.add(participant);
        }
        return participants;
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
