package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.entity.*;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;
    private final SettlementHistoryRepository settlementHistoryRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final SettlementParticipantRepository settlementParticipantRepository;

    /**
     * 참여자가 송금 버튼을 눌러 송금 처리
     */
    @Transactional
    public PaymentHistory processPayment(Long memberId, Long settlementId) {
        Member member = findMemberOrThrow(memberId);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));

        SettlementParticipant sender = settlement.getSettlementParticipants()
                .stream()
                .filter(p -> p.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_A_SETTLEMENT_PARTICIPANT));

        Member receiver = settlement.getPayer();
        if (receiver == null) {
            throw new CustomException(ErrorCode.NOT_THE_SETTLEMENT_PAYER);
        }

        PaymentHistory paymentHistory = PaymentHistory.builder()
                .settlement(settlement)
                .sender(member)
                .receiver(receiver)
                .amount(sender.getShareAmount())
                .transferDate(LocalDateTime.now())
                .status(PaymentStatus.PAID)
                .build();

        try {
            boolean paymentSuccess = true; // 송금 성공
//        boolean paymentSuccess = false; // 송금 실패 가정
            if (paymentSuccess) {
                sender.setStatus(PaymentStatus.PAID);
                settlementParticipantRepository.save(sender);
                // 모든 참여자 상태가 PAID인지 검사
                boolean allPaid = settlement.getSettlementParticipants()
                        .stream()
                        .allMatch(p -> p.getStatus() == PaymentStatus.PAID);
                if (allPaid) {
                    settlement.setStatus(SettlementStatus.COMPLETED);
                    SettlementHistory completionHistory = SettlementHistory.builder()
                            .settlement(settlement)
                            .title(settlement.getTitle())
                            .status(SettlementStatus.COMPLETED)
                            .changedAt(LocalDateTime.now())
                            .build();
                    settlementHistoryRepository.save(completionHistory);
                    settlementRepository.save(settlement);
                }

                paymentHistory.setStatus(PaymentStatus.PAID);
            } else {
                paymentHistory.setStatus(PaymentStatus.FAILED);
            }
        } catch (Exception e) {
            paymentHistory.setStatus(PaymentStatus.FAILED);
            throw e;
        }

        paymentHistoryRepository.save(paymentHistory);

        return paymentHistory;
    }

    /**
     * 나의 송금 히스토리 조회
     * - 경우 1: groupId와 settlementId 모두 있을 때
     * → 특정 그룹 내 특정 정산에 해당하는 송금 내역 조회
     * - 경우 2: settlementId만 있을 때
     * → 특정 정산에 해당하는 송금 내역 조회 (그룹 전체 포함)
     * - 경우 3: groupId만 있을 때
     * → 특정 그룹 내 모든 송금 내역 조회
     * - 경우 4: 기간 내 groupId, settlementId 모두 없을 때
     * → 기간 내 전체 송금 내역 조회
     * - 경우 5: groupId, settlementId 모두 없을 때 (fromDate/toDate가 null)
     * → 전체 송금 내역 조회
     */
    public Page<PaymentHistoryResponse> getPaymentHistories(Long memberId, Long groupId, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Member member = findMemberOrThrow(memberId);
        Page<PaymentHistory> page;

        if (settlementId != null && groupId != null) {
            page = paymentHistoryRepository.findBySenderAndGroupIdAndSettlementIdAndTransferDateBetween(
                    member, groupId, settlementId, fromDate, toDate, pageable);
        } else if (settlementId != null) {
            page = paymentHistoryRepository.findBySenderAndSettlementIdAndTransferDateBetween(
                    member, settlementId, fromDate, toDate, pageable);
        } else if (groupId != null) {
            page = paymentHistoryRepository.findBySenderAndGroupIdAndTransferDateBetween(
                    member, groupId, fromDate, toDate, pageable);
        } else if (fromDate != null && toDate != null) {
            page = paymentHistoryRepository.findBySenderAndTransferDateBetween(
                    member, fromDate, toDate, pageable);
        } else {
            page = paymentHistoryRepository.findBySender(
                    member, pageable);
        }
        return page.map(PaymentHistoryResponse::fromEntity);
    }

    // 회원 엔티티 조회 메서드
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
