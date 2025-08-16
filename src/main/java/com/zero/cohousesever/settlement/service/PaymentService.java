package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.entity.Participant;
import com.zero.cohousesever.settlement.entity.PaymentHistory;
import com.zero.cohousesever.settlement.entity.PaymentStatus;
import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.repository.ParticipantRepository;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ParticipantRepository participantRepository;

    /**
     * 참여자가 송금 버튼을 눌러 송금 처리
     */
    public PaymentHistory processPayment(Long memberId, Long settlementId) throws AccessDeniedException {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new EntityNotFoundException("Settlement not found with id: " + settlementId));

        Participant participant = settlement.getParticipants()
                .stream()
                .filter(p -> p.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Member is not part of the settlement"));

        Participant payeeParticipant = settlement.getParticipants()
                .stream()
                .filter(p -> p.getMember().getId().equals(settlement.getPayer().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Payer participant not found in the settlement"));

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setSettlement(settlement);
        paymentHistory.setSender(participant);
        paymentHistory.setReceiver(payeeParticipant);
        paymentHistory.setAmount(participant.getShareAmount());
        paymentHistory.setTransferDate(LocalDateTime.now());

        try {
            boolean paymentSuccess = true; // 송금 성공
//            boolean paymentSuccess = false; // 송금 실패 가정

            if (paymentSuccess) {
                participant.setStatus(PaymentStatus.PAID);
                participantRepository.save(participant);

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
}
