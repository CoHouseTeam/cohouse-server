package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.file.dto.FileUploadResponse;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.settlement.dto.*;
import com.zero.cohousesever.settlement.service.PaymentService;
import com.zero.cohousesever.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;
    private final PaymentService paymentService;
    private final S3Service s3Service;

    /**
     * 정산 등록
     */
    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestBody CreateSettlementRequest request,
                                                               @RequestParam(name = "file", required = false) MultipartFile file) throws IOException {
        Long payerId = userDetails.getId();
        System.out.println(payerId);
        SettlementResponse settlementResponse = settlementService.createSettlement(payerId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementResponse);
    }

    /**
     * 정산 취소
     */
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> cancelSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();

        settlementService.cancelSettlement(memberId, settlementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 나의 간단 정산 목록 조회 - 메인페이지 전용
     */
    @GetMapping("/my/simple")
    public ResponseEntity<List<SettlementSimpleResponse>> getMySimpleSettlements(@AuthenticationPrincipal CustomUserDetails userDetails
                                                                     ) {
        List<SettlementSimpleResponse> settlements = settlementService.getMySimpleSettlements(userDetails.getId());
        return ResponseEntity.ok(settlements);
    }

    /**
     * 나의 정산 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<SettlementResponse>> getMySettlements(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     Pageable pageable) {
        Page<SettlementResponse> settlements = settlementService.getMySettlements(userDetails.getId(), pageable);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 나의 특정 정산 상세 조회
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();
        SettlementResponse settlement = settlementService.getSettlementDetail(memberId, settlementId);
        return ResponseEntity.ok(settlement);
    }

    /**
     * 그룹의 정산 목록 조회 (그룹장 권한 필요)
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<SettlementResponse>> getGroupSettlements(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                        @PathVariable Long groupId,
                                                                        Pageable pageable) {
        Page<SettlementResponse> settlements = settlementService.getGroupSettlements(userDetails.getId(), groupId, pageable);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 정산 참여자 목록 조회
     */
    @GetMapping("/{settlementId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @PathVariable Long settlementId) {
        Long memberId = userDetails.getId();
        List<ParticipantResponse> participants = settlementService.getSettlementParticipants(memberId, settlementId);
        return ResponseEntity.ok(participants);
    }

    /**
     * 나의 정산 히스토리 조회
     */
    @GetMapping("/my/history")
    public ResponseEntity<Page<SettlementHistoryResponse>> getMySettlementHistory(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                  Pageable pageable) {
        Page<SettlementHistoryResponse> history = settlementService.getMySettlementHistories(userDetails.getId(), pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * 정산 등록 시 금액 추출을 위한 OCR 처리
     */
    @PostMapping("/ocr/receipt")
    public ResponseEntity<FileUploadResponse> extractAmountFromTempReceipt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return ResponseEntity.ok(settlementService.extractAmountFromTempReceipt(file));
    }

    /**
     * 영수증 이미지 업데이트
     * - 기존 이미지 존재하지 않을 시 새 이미지 업로드
     * - 기존 이미지 존재 시 기존 이미지 삭제 후 업로드
     */
    @PostMapping("/{settlementId}/receipt")
    public ResponseEntity<FileUploadResponse> updateReceiptImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long groupId,
            @PathVariable Long settlementId,
            @RequestParam("file") MultipartFile file) throws IOException {

        FileUploadResponse response = settlementService.updateReceiptImage(userDetails.getId(), file, groupId, settlementId);
        return ResponseEntity.ok(response);
    }

    /**
     * 영수증 이미지 삭제
     */
    @DeleteMapping("/{settlementId}/receipt")
    public ResponseEntity<?> deleteReceiptImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long settlementId) throws IOException {

        settlementService.deleteReceiptImage(userDetails.getId(), settlementId);

        return ResponseEntity.noContent().build();
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
