package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.entity.PaymentStatus;
import com.zero.cohousesever.settlement.entity.Settlement;
import com.zero.cohousesever.settlement.entity.SettlementParticipant;
import com.zero.cohousesever.settlement.entity.SettlementStatus;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementParticipantRepository settlementParticipantRepository;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("결제자일 때 정산 취소 시 참여자 상태와 히스토리 정상 변경")
    void cancelSettlement_ByPayer_Success() {
        Long payerId = 1L;
        Long settlementId = 100L;

        Member payer = Member.builder()
                .name("박결제")
                .build();
        payer.setId(payerId);

        Member member1 = Member.builder()
                .name("김참여")
                .build();
        member1.setId(2L);

        Member member2 = Member.builder()
                .name("신참여")
                .build();
        member2.setId(3L);

        SettlementParticipant participantPaid = SettlementParticipant.builder()
                .member(payer)
                .status(PaymentStatus.PAID)
                .shareAmount(10000L)
                .build();

        SettlementParticipant participantPending = SettlementParticipant.builder()
                .member(member1)
                .status(PaymentStatus.PENDING)
                .shareAmount(5000L)
                .build();

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .status(SettlementStatus.PENDING)
                .settlementParticipants(List.of(participantPaid, participantPending))
                .build();
        settlement.setId(settlementId);

        // Mock 동작 정의
        when(memberRepository.findById(payerId)).thenReturn(Optional.of(payer));
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(settlementParticipantRepository.saveAll(anyList())).thenReturn(null); // void 대체
        when(settlementRepository.save(any())).thenReturn(settlement);
        when(paymentHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0)); // 저장한 객체 그대로 반환

        // 실행
        settlementService.cancelSettlement(payerId, settlementId);

        // 검증
        // 결제자는 PAID → REFUNDED 상태가 됨
        assertEquals(PaymentStatus.REFUNDED, participantPaid.getStatus());

        // 대기중인 참여자는 PENDING → CANCELED 상태가 됨
        assertEquals(PaymentStatus.CANCELED, participantPending.getStatus());

        // 정산 상태가 CANCELED로 변경되었는지 확인
        assertEquals(SettlementStatus.CANCELED, settlement.getStatus());

        // Repository 저장 호출 여부 검증
        verify(settlementParticipantRepository, times(1)).saveAll(anyList());
        verify(settlementRepository, times(1)).save(settlement);
        verify(paymentHistoryRepository, atLeastOnce()).save(any());
    }
}