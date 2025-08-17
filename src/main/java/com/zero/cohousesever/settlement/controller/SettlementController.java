package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementResponseDto;
import com.zero.cohousesever.settlement.service.PaymentService;
import com.zero.cohousesever.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;
    private final PaymentService paymentService;

     // 정산 등록
    @PostMapping
    public ResponseEntity<SettlementResponseDto> createSettlement(@RequestBody CreateSettlementRequest request) {
        //TODO JWT 사용자 ID로 결제자 선정
        Long payerId = 2L;
        SettlementResponseDto settlementResponseDto = settlementService.createSettlement(payerId, request);
        return ResponseEntity.ok(settlementResponseDto);
    }

    // 정산 취소
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> cancelSettlement(@PathVariable Long settlementId) {
        //TODO JWT 회원 정보 받기
        Long memberId = 2L;
        settlementService.cancelSettlement(memberId, settlementId);
        return ResponseEntity.ok().build();
    }

    // 정산 목록 조회
    @GetMapping
    public ResponseEntity<?> getSettlements() {

    }

    // 정산 상세 조회
    @GetMapping("/{settlementId}")
    public ResponseEntity<?> getSettlement() {

        return ResponseEntity.ok().build();
    }

    // 정산 참여자 목록 조회
    @GetMapping("/{settlementId}/participants")
    public ResponseEntity<?> getParticipants() {

        return ResponseEntity.ok().build();
    }

    // FIXME 프론트에서 한번에 MAP으로 데이터 넘겨주기에 필요없음
//    // 정산 참여자 추가
//    @PostMapping("/{settlementId}/participants")
//    public ResponseEntity<?> addParticipant() {
//
//        return ResponseEntity.ok().build();
//    }
//
//    // 정산 참여자 제거
//    @DeleteMapping("/{settlementId}/participants/{participantId}")
//    public ResponseEntity<?> removeParticipant() {
//
//        return ResponseEntity.ok().build();
//    }

    // 영수증 이미지 업로드
    @PostMapping("/{settlementId}/image")
    public ResponseEntity<?> uploadReceiptImage() {

        return ResponseEntity.ok().build();
    }

    // 송금 완료 처리
    @PostMapping("/{settlementId}/payment")
    public ResponseEntity<?> completePayment(
            @PathVariable Long settlementId) throws AccessDeniedException {
        //FIXME JWT에서 회원 ID 추출하여 사용
        //Long memberId = userDetails.getId();
        Long memberId = 3L;

        paymentService.processPayment(memberId, settlementId);
        return ResponseEntity.ok().build();
    }
}
