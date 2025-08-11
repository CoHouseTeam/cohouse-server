package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.service.PaymentService;
import com.zero.cohousesever.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;
    private final PaymentService paymentService;

    // TODO: 실제 구현 필요

    // 정산 목록 조회
    @GetMapping
    public ResponseEntity<?> getSettlements() {

        return ResponseEntity.ok().build();
    }

    // 정산 등록
    @PostMapping
    public ResponseEntity<?> createSettlement() {

        return ResponseEntity.ok().build();
    }

    // 정산 상세 조회
    @GetMapping("/{settlementId}")
    public ResponseEntity<?> getSettlement() {

        return ResponseEntity.ok().build();
    }

    // 정산 취소
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> cancelSettlement() {

        return ResponseEntity.ok().build();
    }

    // 정산 참여자 목록 조회
    @GetMapping("/{settlementId}/participants")
    public ResponseEntity<?> getParticipants() {

        return ResponseEntity.ok().build();
    }

    // 정산 참여자 추가
    @PostMapping("/{settlementId}/participants")
    public ResponseEntity<?> addParticipant() {

        return ResponseEntity.ok().build();
    }

    // 정산 참여자 제거
    @DeleteMapping("/{settlementId}/participants/{participantId}")
    public ResponseEntity<?> removeParticipant() {

        return ResponseEntity.ok().build();
    }

    // 영수증 이미지 업로드
    @PostMapping("/{settlementId}/image")
    public ResponseEntity<?> uploadReceiptImage() {

        return ResponseEntity.ok().build();
    }


    // 그룹 전체 정산 히스토리 조회
    @GetMapping("/histories")
    public ResponseEntity<List<?>> getGroupSettlementHistories(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ResponseEntity.ok(
                settlementService.getGroupSettlementHistories(groupId, memberId, fromDate, toDate)
        );
    }

    // 특정 정산 히스토리 조회
    @GetMapping("/{settlementId}/histories")
    public ResponseEntity<List<SettlementHistoryResponse>> getSettlementHistories(
            @PathVariable Long groupId,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(
                settlementService.getSettlementHistories(groupId, settlementId)
        );
    }

    // 특정 정산의 내 송금 내역 조회
    @GetMapping("/{settlementId}/payments")
    public ResponseEntity<List<PaymentHistoryResponse>> getSettlementPaymentHistories(
            @PathVariable Long groupId,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(
                paymentService.getMyPaymentHistoriesInSettlement(groupId, settlementId)
        );
    }

    // 해당 그룹의 내 송금 내역 조회 (필터 optional)
    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryResponse>> getMyPaymentsInGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long settlementId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ResponseEntity.ok(
                paymentService.getMyPaymentsInGroup(groupId, settlementId, status, fromDate, toDate)
        );
    }
}
