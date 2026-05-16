package com.example.loan.repository;

import com.example.loan.entity.LoanApplication;
import com.example.loan.entity.LoanApplication.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserId(Long userId);
    List<LoanApplication> findByStatus(ApplicationStatus status);
    List<LoanApplication> findByHighRiskAndManualReviewed(Boolean highRisk, Boolean manualReviewed);
    List<LoanApplication> findByStatusIn(List<ApplicationStatus> statuses);
}