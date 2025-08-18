package com.zero.cohousesever.settlement.service;


import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.dto.SettlementResponseDto;
import com.zero.cohousesever.settlement.entity.*;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlementService {
    public final MemberRepository memberRepository;
    public final GroupMemberRepository groupMemberRepository;
    public final SettlementParticipantRepository settlementParticipantRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementHistoryRepository settlementHistoryRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    /**
     * 정산 등록
     */
    public SettlementResponseDto createSettlement(Long payerId, CreateSettlementRequest request) {
        Member payer = memberRepository.findById(payerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Group group = groupMemberRepository
                .findByMemberIdAndStatus(payerId, GroupMemberStatus.ACTIVE)
                .map(GroupMember::getGroup)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        Settlement settlement = Settlement.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .settlementAmount(request.getSettlementAmount())
                .status(SettlementStatus.PENDING)
                .payer(payer)
                .group(group)
                .build();

        Set<Long> allParticipantIds = new HashSet<>(request.getParticipantIds());
        allParticipantIds.add(payerId); // 결제자 포함

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        if (request.isEqualDistribution()) {
            settlementParticipants = createEqualDistributionParticipants(settlement, allParticipantIds, request.getSettlementAmount());
        } else {
            settlementParticipants = createManualDistributionParticipants(settlement, allParticipantIds, request.getManualShares(), request.getSettlementAmount());
        }

        settlement.setSettlementParticipants(settlementParticipants);
        Settlement savedSettlement = settlementRepository.save(settlement);

        SettlementHistory history = SettlementHistory.builder()
                .settlement(savedSettlement)
                .changedBy(payer)
                .title(savedSettlement.getTitle())
                .status(savedSettlement.getStatus())
                .changedAt(LocalDateTime.now())
                .build();
        settlementHistoryRepository.save(history);

        return SettlementResponseDto.fromEntity(savedSettlement);
    }

    // 균등 분배 참여자 생성 메서드
    private List<SettlementParticipant> createEqualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Long totalAmount) {
        Long shareAmount = calculateShareAmount(totalAmount, participantIds.size());
        Long remainder = totalAmount % participantIds.size();
        settlement.setPlatformSupportAmount(remainder);

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = findMemberOrThrow(memberId);

            SettlementParticipant settlementParticipant = SettlementParticipant.builder()
                    .member(member)
                    .settlement(settlement)
                    .status(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING)
                    .shareAmount(shareAmount)
                    .build();
            settlementParticipants.add(settlementParticipant);
        }
        return settlementParticipants;
    }

    // 수동 분배 참여자 생성 메서드
    private List<SettlementParticipant> createManualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Map<Long, Long> manualShares, Long totalAmount) {
        if (manualShares == null || manualShares.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MANUAL_DISTRIBUTION);
        }

        // 참여자들의 금액의 합이 맞는지 계산
        Long sumShares = manualShares.values().stream().mapToLong(Long::longValue).sum();
        Long payerId = settlement.getPayer().getId();

        // 결제자 부담금 계산
        Long payerShare = totalAmount - sumShares;

        // 분배 금액 합이 정산 금액을 초과하면 예외 처리
        if (payerShare < 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_AMOUNT);
        }

        manualShares.put(payerId, payerShare);

        settlement.setPlatformSupportAmount(0L); // 플랫폼 오차 지원금 없음

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = findMemberOrThrow(memberId);
            SettlementParticipant settlementParticipant = new SettlementParticipant();
            settlementParticipant.setMember(member);
            settlementParticipant.setSettlement(settlement);
            settlementParticipant.setStatus(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING);
            settlementParticipant.setShareAmount(manualShares.getOrDefault(memberId, 0L));
            settlementParticipants.add(settlementParticipant);
        }
        return settlementParticipants;
    }

    // 배분 금액 계산 메서드
    public Long calculateShareAmount(Long totalAmount, int participantCount) {
        if (participantCount <= 0) {
            throw new CustomException(ErrorCode.INVALID_PARTICIPANT_COUNT);
        }
        return totalAmount / participantCount;
    }

    /**
     * 정산 취소 처리
     *
     * - 정산 취소 시 송금을 한 정산 참여자만 환불 상태로 변경
     */
    @Transactional
    public void cancelSettlement(Long memberId, Long settlementId) {
        Member member = findMemberOrThrow(memberId);
        Settlement settlement = findSettlementOrThrow(settlementId);

        PaymentHistory paymentHistory = paymentHistoryRepository
                .findBySenderAndSettlementId(member, settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_HISTORY_NOT_FOUND));

        settlement.setStatus(SettlementStatus.CANCELED);
        settlementRepository.save(settlement);

        if (paymentHistory.getStatus() == PaymentStatus.PAID) {
            paymentHistory.setStatus(PaymentStatus.REFUNDED);
            paymentHistoryRepository.save(paymentHistory);
        }

        // 정산 참여자 상태도 함께 업데이트
        settlement.getSettlementParticipants()
                .stream()
                .filter(p -> p.getMember().getId().equals(memberId))
                .forEach(p -> {
                    if (p.getStatus() == PaymentStatus.PAID) {
                        p.setStatus(PaymentStatus.REFUNDED);
                    } else if (p.getStatus() == PaymentStatus.PENDING) {
                        p.setStatus(PaymentStatus.CANCELED);
                    }
                });
        settlementParticipantRepository.saveAll(settlement.getSettlementParticipants());
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
     * 정산 참여자 목록 조회
     */
    public void getParticipants() {
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

    // 회원 엔티티 조회 메서드
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 정산 엔티티 조회 메서드
    private Settlement findSettlementOrThrow(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));
    }
}
