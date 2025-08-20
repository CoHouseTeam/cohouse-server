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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    public PaymentHistory processPayment(Long memberId, Long settlementId) throws AccessDeniedException {
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
        }

        paymentHistoryRepository.save(paymentHistory);

        return paymentHistory;
    }

    /**
     * 나의 송금 히스토리 조회
     */
    public List<PaymentHistoryResponse> getPaymentHistories(Long memberId, Long groupId, Long settlementId, LocalDateTime fromDate, LocalDateTime toDate) {
        Member member = findMemberOrThrow(memberId);

        if (settlementId != null && groupId != null) {
            // 그룹 내 특정 정산에 해당하는 나의 송금 내역 조회
            return paymentHistoryRepository.findBySenderAndGroupIdAndSettlementIdAndTransferDateBetween(member, groupId, settlementId, fromDate, toDate)
                    .stream()
                    .map(PaymentHistoryResponse::fromEntity)
                    .toList();
        } else if (settlementId != null) {
            // 특정 정산에 관한 나의 송금 내역 조회
            return paymentHistoryRepository.findBySenderAndSettlementIdAndTransferDateBetween(member, settlementId,  fromDate, toDate)
                    .stream()
                    .map(PaymentHistoryResponse::fromEntity)
                    .toList();
        } else if (groupId != null) {
            // 그룹 내 나의 전체 송금 내역 조회
            return paymentHistoryRepository.findBySenderAndGroupIdAndTransferDateBetween(member, groupId,  fromDate, toDate)
                    .stream()
                    .map(PaymentHistoryResponse::fromEntity)
                    .toList();
        } else {
            // 나의 전체 송금 내역 조회
            return paymentHistoryRepository.findBySenderAndTransferDateBetween(member,  fromDate, toDate)
                    .stream()
                    .map(PaymentHistoryResponse::fromEntity)
                    .toList();
        }
    }

    // 회원 엔티티 조회 메서드
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
