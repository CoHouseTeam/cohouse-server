package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.ParticipantResponse;
import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.dto.SettlementResponse;
import com.zero.cohousesever.settlement.service.PaymentService;
import com.zero.cohousesever.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;
    private final PaymentService paymentService;

    // 정산 등록
    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestBody CreateSettlementRequest request) {
        Long payerId = userDetails.getId();
        System.out.println(payerId);
        SettlementResponse settlementResponse = settlementService.createSettlement(payerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementResponse);
    }

    // 정산 취소
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> cancelSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();

        settlementService.cancelSettlement(memberId, settlementId);
        return ResponseEntity.noContent().build();
    }

    // 나의 정산 목록 조회
    @GetMapping("/my")
    public ResponseEntity<Page<SettlementResponse>> getMySettlements(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     Pageable pageable) {
        Page<SettlementResponse> settlements = settlementService.getMySettlements(userDetails.getId(), pageable);
        return ResponseEntity.ok(settlements);
    }

    // 나의 특정 정산 상세 조회
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();
        SettlementResponse settlement = settlementService.getSettlementDetail(memberId, settlementId);
        return ResponseEntity.ok(settlement);
    }

    // 그룹의 정산 목록 조회(그룹장)
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<SettlementResponse>> getGroupSettlements(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                        @PathVariable Long groupId,
                                                                        Pageable pageable) {
        Page<SettlementResponse> settlements = settlementService.getGroupSettlements(userDetails.getId(), groupId, pageable);
        return ResponseEntity.ok(settlements);
    }

    // 정산 참여자 목록 조회
    @GetMapping("/{settlementId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();
        List<ParticipantResponse> participants = settlementService.getSettlementParticipants(memberId, settlementId);
        return ResponseEntity.ok(participants);
    }

    // 나의 정산 히스토리 조회
    @GetMapping("/my/history")
    public ResponseEntity<List<SettlementHistoryResponse>> getMySettlementHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SettlementHistoryResponse> history = settlementService.getMySettlementHistories(userDetails.getId());
        return ResponseEntity.ok(history);
    }

    // 영수증 이미지 업로드
    @PostMapping("/{settlementId}/image")
    public ResponseEntity<?> uploadReceiptImage(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok().build();
    }

    // 송금 완료 처리
    @PostMapping("/{settlementId}/payment")
    public ResponseEntity<?> completePayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable Long settlementId) throws AccessDeniedException {
        Long memberId = userDetails.getId();

        paymentService.processPayment(memberId, settlementId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
