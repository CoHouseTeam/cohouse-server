package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.SettlementResponseDto;
import com.zero.cohousesever.settlement.service.PaymentService;
import com.zero.cohousesever.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<SettlementResponseDto> createSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @RequestBody CreateSettlementRequest request) {
//        Long payerId = userDetails.getId();
        Long payerId = 2L;

        SettlementResponseDto settlementResponseDto = settlementService.createSettlement(payerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementResponseDto);
    }

    // 정산 취소
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> cancelSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long settlementId) {
//        Long memberId = userDetails.getId();
        Long memberId = 2L;

        settlementService.cancelSettlement(memberId, settlementId);
        return ResponseEntity.noContent().build();
    }

    // 정산 목록 조회
    @GetMapping
    public ResponseEntity<?> getSettlements(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().build();
    }

    // 정산 상세 조회
    @GetMapping("/{settlementId}")
    public ResponseEntity<?> getSettlement(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok().build();
    }

    // 정산 참여자 목록 조회
    @GetMapping("/{settlementId}/participants")
    public ResponseEntity<?> getParticipants(@AuthenticationPrincipal CustomUserDetails userDetails) {

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
    public ResponseEntity<?> uploadReceiptImage(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok().build();
    }

    // 송금 완료 처리
    @PostMapping("/{settlementId}/payment")
    public ResponseEntity<?> completePayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable Long settlementId) throws AccessDeniedException {
//        Long memberId = userDetails.getId();
        Long memberId = 4L;

        paymentService.processPayment(memberId, settlementId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
