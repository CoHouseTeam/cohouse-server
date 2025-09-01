package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementResponse;
import com.zero.cohousesever.settlement.entity.*;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementParticipantRepository settlementParticipantRepository;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private SettlementHistoryRepository settlementHistoryRepository;

    @InjectMocks
    private SettlementService settlementService;

    @InjectMocks
    private PaymentService paymentService;

    private Member payer;
    private Member participant1;
    private Member participant2;
    private Member leader;
    private Group group;
    private GroupMember activeGroupMember;
    private GroupMember leaderGroupMember;

    @BeforeEach
    void setUp() {
        payer = Member.builder().name("김민수").build();
        ReflectionTestUtils.setField(payer, "id", 1L);

        participant1 = Member.builder().name("박영희").build();
        ReflectionTestUtils.setField(participant1, "id", 2L);

        participant2 = Member.builder().name("이철수").build();
        ReflectionTestUtils.setField(participant2, "id", 3L);

        leader = Member.builder().name("홍길동").build();
        ReflectionTestUtils.setField(leader, "id", 4L);

        group = Group.builder().name("테스트 그룹").build();
        ReflectionTestUtils.setField(group, "id", 1L);

        // 그룹 멤버들
        activeGroupMember = GroupMember.builder()
                .member(payer)
                .group(group)
                .status(GroupMemberStatus.ACTIVE)
                .isLeader(false)   // 일반 멤버
                .build();

        leaderGroupMember = GroupMember.builder()
                .member(leader)
                .group(group)
                .status(GroupMemberStatus.ACTIVE)
                .isLeader(true)    // 그룹장
                .build();
    }

    @Test
    @DisplayName("정산 생성 성공 - 균등 분배")
    void createSettlement_Success_EqualDistribution() throws IOException {
        // Given
        Long payerId = 1L;
        CreateSettlementRequest request = CreateSettlementRequest.builder()
                .title("Test Settlement")
                .description("Test Description")
                .category(SettlementCategory.CULTURE)
                .settlementAmount(30000L)
                .equalDistribution(true)
                .participantIds(List.of(2L, 3L))
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(participant1));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(participant2));
        when(groupMemberRepository.findByMemberIdAndStatus(payerId, GroupMemberStatus.ACTIVE))
                .thenReturn(Optional.of(activeGroupMember));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            ReflectionTestUtils.setField(settlement, "id", 1L);
            return settlement;
        });
        when(settlementHistoryRepository.save(any(SettlementHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SettlementResponse responseDto = settlementService.createSettlement(payerId, request, null);

        // Then
        assertAll("정산 생성 결과 검증",
                () -> assertEquals(request.getTitle(), responseDto.getTitle()),
                () -> assertEquals(request.getDescription(), responseDto.getDescription()),
                () -> assertEquals(request.getCategory(), responseDto.getCategory()),
                () -> assertEquals(request.getSettlementAmount(), responseDto.getSettlementAmount()),
                () -> assertEquals(SettlementStatus.PENDING, responseDto.getStatus()),
                () -> assertEquals(payerId, responseDto.getPayerId()),
                () -> assertEquals(3, responseDto.getParticipants().size()),
                () -> assertTrue(responseDto.isEqualDistribution())
        );

        // 각 참여자의 분담금 검증 (30000 / 3 = 10000)
        responseDto.getParticipants().forEach(participant -> {
            assertEquals(10000L, participant.getShareAmount());
        });

        // 플랫폼 지원금 검증 (30000 % 3 = 0)
        assertEquals(0L, responseDto.getPlatformSupportAmount());
    }

    @Test
    @DisplayName("결제자일 때 정산 취소 시 참여자 상태와 히스토리 상태 'REFUNDED'로 변경")
    void cancelSettlement_ByPayer_Success() {
        Long payerId = 1L;
        Long settlementId = 100L;

        SettlementParticipant participantPaid = SettlementParticipant.builder()
                .member(participant1)
                .status(PaymentStatus.PAID)
                .shareAmount(10000L)
                .build();

        SettlementParticipant participantPending = SettlementParticipant.builder()
                .member(participant2)
                .status(PaymentStatus.PENDING)
                .shareAmount(5000L)
                .build();

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .status(SettlementStatus.PENDING)
                .settlementParticipants(List.of(participantPaid, participantPending))
                .build();
        ReflectionTestUtils.setField(settlement, "id", settlementId);

        when(memberRepository.findById(payerId)).thenReturn(Optional.of(payer));
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(settlementParticipantRepository.saveAll(anyList())).thenReturn(null);
        when(settlementRepository.save(any())).thenReturn(settlement);
        when(paymentHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        settlementService.cancelSettlement(payerId, settlementId);

        // then
        assertEquals(PaymentStatus.REFUNDED, participantPaid.getStatus());
        assertEquals(PaymentStatus.CANCELED, participantPending.getStatus());
        assertEquals(SettlementStatus.CANCELED, settlement.getStatus());
    }

    @Test
    @DisplayName("송금 처리 중 예외 발생 시 참여자와 PaymentHistory 상태가 FAILED로 변경됨")
    void transfer_Exception_SetsFailedStatusForParticipant() throws AccessDeniedException {
        Long settlementId = 100L;

        SettlementParticipant participant = SettlementParticipant.builder()
                .member(participant1)
                .status(PaymentStatus.PENDING)
                .shareAmount(10000L)
                .build();

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .status(SettlementStatus.PENDING)
                .settlementParticipants(List.of(participant))
                .build();
        ReflectionTestUtils.setField(settlement, "id", settlementId);

        when(memberRepository.findById(participant1.getId())).thenReturn(Optional.of(participant1));
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        // settlementParticipantRepository.save 호출 시 예외 발생
        doThrow(new RuntimeException("DB 저장 실패"))
                .when(settlementParticipantRepository).save(any());

        PaymentHistory paymentHistory = paymentService.processPayment(participant1.getId(), settlementId);

        // try-catch 안에서 예외 발생 → catch 블록 수행
        assertEquals(PaymentStatus.FAILED, paymentHistory.getStatus());
    }

    @Test
    @DisplayName("나의 정산 목록 조회 - 성공")
    void getMySettlements_success() {
        // given
        Settlement settlement = Settlement.builder()
                .group(group)
                .payer(payer)
                .title("회식 정산")
                .build();
        ReflectionTestUtils.setField(settlement, "id", 3L);

        SettlementParticipant settlementParticipant = SettlementParticipant.builder()
                .settlement(settlement)
                .member(participant1)
                .shareAmount(10000L)
                .status(PaymentStatus.PENDING)
                .build();

        settlement.setSettlementParticipants(List.of(settlementParticipant));

        Page<Settlement> page = new PageImpl<>(List.of(settlement), Pageable.unpaged(), 1);

        given(memberRepository.findById(1L)).willReturn(Optional.of(payer));
        given(settlementRepository.findAllByParticipantMember(any(Member.class), any(Pageable.class)))
                .willReturn(page);

        // when
        Page<SettlementResponse> result = settlementService.getMySettlements(1L, Pageable.unpaged());

        // then
        SettlementResponse res = result.getContent().get(0);
        assertThat(res.getId()).isEqualTo(3L);
        assertThat(res.getTitle()).isEqualTo("회식 정산");
        assertThat(res.getPayerName()).isEqualTo("김민수");
    }

    @Test
    @DisplayName("그룹 정산 목록 조회 - 그룹장 성공")
    void getGroupSettlements_asLeader_success() {
        // given
        Settlement settlement = Settlement.builder()
                .group(group)
                .payer(payer)
                .title("회식 정산")
                .build();
        ReflectionTestUtils.setField(settlement, "id", 5L);

        SettlementParticipant settlementParticipant = SettlementParticipant.builder()
                .settlement(settlement)
                .member(participant2)
                .shareAmount(10000L)
                .status(PaymentStatus.PENDING)
                .build();
        settlement.setSettlementParticipants(List.of(settlementParticipant));

        Page<Settlement> page = new PageImpl<>(List.of(settlement), Pageable.unpaged(), 1);

        given(memberRepository.findById(4L)).willReturn(Optional.of(leader));
        given(groupRepository.findById(1L)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupAndMemberAndIsLeaderTrue(group, leader)).willReturn(true);
        given(settlementRepository.findAllByGroup(eq(group), any(Pageable.class)))
                .willReturn(page);

        // when
        Page<SettlementResponse> result = settlementService.getGroupSettlements(4L, 1L, Pageable.unpaged());

        // then
        SettlementResponse res = result.getContent().get(0);
        assertThat(res.getTitle()).isEqualTo("회식 정산");
    }


    @Test
    @DisplayName("그룹 정산 목록 조회 - 그룹장이 아님")
    void getGroupSettlements_notLeader_fail() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(payer));
        given(groupRepository.findById(1L)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupAndMemberAndIsLeaderTrue(group, payer))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> settlementService.getGroupSettlements(1L, 1L, Pageable.unpaged()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_GROUP_LEADER.getMessage());
    }
}