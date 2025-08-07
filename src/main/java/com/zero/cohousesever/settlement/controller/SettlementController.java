package com.zero.cohousesever.settlement.controller;

import com.zero.cohousesever.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    // TODO: 실제 구현 필요
    @GetMapping
    public ResponseEntity<String> getSettlements() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<String> createSettlement(@RequestBody CreateSettlementRequest request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<String> getSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{settlementId}")
    public ResponseEntity<String> updateSettlement(@PathVariable Long settlementId, @RequestBody UpdateSettlementRequest request) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{settlementId}")
    public ResponseEntity<String> deleteSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok().build();
    }
}
