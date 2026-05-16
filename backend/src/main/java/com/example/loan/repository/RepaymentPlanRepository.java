package com.example.loan.repository;

import com.example.loan.entity.RepaymentPlan;
import com.example.loan.entity.RepaymentPlan.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentPlanRepository extends JpaRepository<RepaymentPlan, Long> {
    List<RepaymentPlan> findByLoanApplicationId(Long loanApplicationId);
    List<RepaymentPlan> findByStatus(RepaymentStatus status);
    List<RepaymentPlan> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    List<RepaymentPlan> findByStatusAndDueDateLessThan(RepaymentStatus status, LocalDate date);
    List<RepaymentPlan> findByLoanApplicationUserId(Long userId);
}