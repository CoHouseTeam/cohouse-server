package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.dto.PaymentHistorySearchRequest;
import com.zero.cohousesever.settlement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 내 결제 히스토리 조회 (필터 optional)
     * - groupId: 특정 그룹 내 결제
     * - settlementId: 특정 정산 내 결제
     * - fromDate/toDate: 기간 필터
     */
    @GetMapping("/histories")
    public ResponseEntity<Page<PaymentHistoryResponse>> getPaymentHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails
            , @ModelAttribute PaymentHistorySearchRequest request,
            Pageable pageable) {
        Page<PaymentHistoryResponse> payments = paymentService.getPaymentHistories(
                userDetails.getId(),
                request.getGroupId(),
                request.getSettlementId(),
                request.getFromDateTime(),
                request.getToDateTime(),
                pageable);

        return ResponseEntity.ok(payments);
    }
}