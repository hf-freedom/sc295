package com.example.loan.controller;

import com.example.loan.dto.request.LoanApplicationRequest;
import com.example.loan.dto.response.LoanApplicationResponse;
import com.example.loan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> applyForLoan(@RequestBody LoanApplicationRequest request) {
        LoanApplicationResponse response = loanService.applyForLoan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationResponse> getApplication(@PathVariable Long applicationId) {
        List<LoanApplicationResponse> applications = loanService.getAllApplications();
        return applications.stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> getAllApplications() {
        List<LoanApplicationResponse> response = loanService.getAllApplications();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanApplicationResponse>> getApplicationsByUser(@PathVariable Long userId) {
        List<LoanApplicationResponse> response = loanService.getApplicationsByUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<LoanApplicationResponse>> getHighRiskApplications() {
        List<LoanApplicationResponse> response = loanService.getHighRiskApplications();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationId}/review")
    public ResponseEntity<LoanApplicationResponse> reviewApplication(
            @PathVariable Long applicationId, 
            @RequestBody Map<String, Object> request) {
        boolean approve = (Boolean) request.get("approve");
        String reason = (String) request.get("reason");
        LoanApplicationResponse response = loanService.reviewApplication(applicationId, approve, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationId}/disburse")
    public ResponseEntity<LoanApplicationResponse> disburseLoan(@PathVariable Long applicationId) {
        LoanApplicationResponse response = loanService.disburseLoan(applicationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auto-review")
    public ResponseEntity<Map<String, String>> autoReview() {
        loanService.autoReviewApplications();
        return ResponseEntity.ok(Map.of("message", "自动审核完成"));
    }
}