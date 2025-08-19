package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.entity.SettlementParticipant;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import com.zero.cohousesever.settlement.entity.PaymentStatus;
import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final SettlementParticipantRepository settlementParticipantRepository;

    /**
     * 참여자가 송금 버튼을 눌러 송금 처리
     */
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
//            boolean paymentSuccess = false; // 송금 실패 가정

            if (paymentSuccess) {
                sender.setStatus(PaymentStatus.PAID);
                settlementParticipantRepository.save(sender);

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

    public List<PaymentHistoryResponse> getPaymentHistories(Long groupId, Long settlementId) {
        return null;
    }

    public List<PaymentHistoryResponse> getMyPaymentHistoriesInSettlement(Long groupId, Long settlementId) {
        return null;
    }

    //FIXME status ENUM으로 변경
    public List<PaymentHistoryResponse> getMyPaymentsInGroup(Long groupId, Long SettlementId, String status, LocalDate fromDate, LocalDate toDate) {
        return null;
    }

    // 회원 엔티티 조회 메서드
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
