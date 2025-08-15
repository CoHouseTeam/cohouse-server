package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.settlement.dto.PaymentCompleteRequest;
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

    // 송금 완료 처리
    @PostMapping("/{settlementId}/payment")
    public ResponseEntity<?> completePayment(
            @PathVariable Long settlementId,
            @RequestBody PaymentCompleteRequest request) {

        return ResponseEntity.ok().build();
    }
}
