package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.settlement.dto.PaymentHistoryResponse;
import com.zero.cohousesever.settlement.dto.PaymentHistorySearchRequest;
import com.zero.cohousesever.settlement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 내 결제 내역 조회 (필터 optional)
     * - groupId: 특정 그룹 내 결제
     * - settlementId: 특정 정산 내 결제
     * - status: 결제 상태 필터
     * - fromDate/toDate: 기간 필터
     */
    @GetMapping("/histories")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails
            , @ModelAttribute PaymentHistorySearchRequest request) {
        List<PaymentHistoryResponse> payments = paymentService.getPaymentHistories(
                userDetails.getId(),
                request.getGroupId(),
                request.getSettlementId(),
                request.getFromDateTime(),  // LocalDateTime 변환된 값 사용
                request.getToDateTime());

        return ResponseEntity.ok(payments);
    }
}
