package com.example.loan.controller;

import com.example.loan.dto.request.RepaymentRequest;
import com.example.loan.dto.response.RepaymentPlanResponse;
import com.example.loan.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    @Autowired
    private RepaymentService repaymentService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RepaymentPlanResponse>> getRepaymentsByUser(@PathVariable Long userId) {
        List<RepaymentPlanResponse> response = repaymentService.getPlansByUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<RepaymentPlanResponse>> getRepaymentsByApplication(@PathVariable Long applicationId) {
        List<RepaymentPlanResponse> response = repaymentService.getPlansByApplication(applicationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay")
    public ResponseEntity<RepaymentPlanResponse> makePayment(@RequestBody RepaymentRequest request) {
        RepaymentPlanResponse response = repaymentService.makePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-overdue")
    public ResponseEntity<Void> processOverdue() {
        repaymentService.processOverdue();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recover-credit")
    public ResponseEntity<Void> recoverCredit() {
        repaymentService.recoverCreditLimit();
        return ResponseEntity.ok().build();
    }
}