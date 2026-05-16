package com.example.loan.service;

import com.example.loan.dto.request.RepaymentRequest;
import com.example.loan.dto.response.RepaymentPlanResponse;
import com.example.loan.entity.LoanApplication;
import com.example.loan.entity.RepaymentPlan;
import com.example.loan.entity.RepaymentPlan.RepaymentStatus;
import com.example.loan.entity.User;
import com.example.loan.exception.BusinessException;
import com.example.loan.repository.LoanApplicationRepository;
import com.example.loan.repository.RepaymentPlanRepository;
import com.example.loan.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentService.class);
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.12");
    private static final int DEFAULT_INSTALLMENTS = 12;

    @Autowired
    private RepaymentPlanRepository repaymentPlanRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    public void generateRepaymentPlan(LoanApplication application) {
        BigDecimal principal = application.getApprovedAmount();
        BigDecimal monthlyInterestRate = ANNUAL_INTEREST_RATE.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyInterestRate, DEFAULT_INSTALLMENTS);
        
        LocalDate currentDate = LocalDate.now();
        
        BigDecimal remainingPrincipal = principal;
        
        for (int i = 1; i <= DEFAULT_INSTALLMENTS; i++) {
            BigDecimal interestPayment = remainingPrincipal.multiply(monthlyInterestRate);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            
            if (i == DEFAULT_INSTALLMENTS) {
                principalPayment = remainingPrincipal;
            }
            
            remainingPrincipal = remainingPrincipal.subtract(principalPayment);
            
            RepaymentPlan plan = new RepaymentPlan();
            plan.setLoanApplication(application);
            plan.setInstallmentNumber(i);
            plan.setDueDate(currentDate.plusMonths(i));
            plan.setPrincipalAmount(principalPayment.setScale(2, RoundingMode.HALF_UP));
            plan.setInterestAmount(interestPayment.setScale(2, RoundingMode.HALF_UP));
            plan.setTotalAmount(principalPayment.add(interestPayment).setScale(2, RoundingMode.HALF_UP));
            plan.setStatus(RepaymentStatus.PENDING);
            
            repaymentPlanRepository.save(plan);
        }
        
        logger.info("还款计划生成成功，共{}期", DEFAULT_INSTALLMENTS);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int installments) {
        BigDecimal ratePlusOne = monthlyRate.add(BigDecimal.ONE);
        BigDecimal power = ratePlusOne.pow(installments);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public RepaymentPlanResponse makePayment(RepaymentRequest request) {
        RepaymentPlan plan = repaymentPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new BusinessException("还款计划不存在"));

        if (plan.getStatus() == RepaymentStatus.PAID) {
            throw new BusinessException("该期还款已完成");
        }

        if (request.getAmount().compareTo(plan.getTotalAmount()) < 0) {
            throw new BusinessException("还款金额不足");
        }

        plan.setStatus(request.getEarlyRepayment() ? RepaymentStatus.EARLY_PAID : RepaymentStatus.PAID);
        plan.setPaidDate(LocalDate.now());
        plan.setPaidAmount(request.getAmount());

        RepaymentPlan saved = repaymentPlanRepository.save(plan);
        
        updateUserCreditLimit(saved);
        
        if (request.getEarlyRepayment()) {
            handleEarlyRepayment(plan.getLoanApplication());
        }
        
        logger.info("还款成功: 计划ID={}, 金额={}", request.getPlanId(), request.getAmount());
        
        return convertToResponse(saved);
    }

    @Transactional
    public void handleEarlyRepayment(LoanApplication application) {
        List<RepaymentPlan> remainingPlans = repaymentPlanRepository
                .findByLoanApplicationId(application.getId())
                .stream()
                .filter(p -> p.getStatus() == RepaymentStatus.PENDING)
                .collect(Collectors.toList());

        BigDecimal totalRemainingPrincipal = remainingPlans.stream()
                .map(RepaymentPlan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountFactor = new BigDecimal("0.95");
        BigDecimal adjustedInterest = totalRemainingPrincipal
                .multiply(ANNUAL_INTEREST_RATE)
                .multiply(discountFactor)
                .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        for (RepaymentPlan plan : remainingPlans) {
            plan.setInterestAmount(adjustedInterest);
            plan.setTotalAmount(plan.getPrincipalAmount().add(adjustedInterest));
            repaymentPlanRepository.save(plan);
        }
        
        logger.info("提前还款利息调整完成: 申请ID={}", application.getId());
    }

    private void updateUserCreditLimit(RepaymentPlan plan) {
        User user = plan.getLoanApplication().getUser();
        user.setUsedCreditLimit(user.getUsedCreditLimit().subtract(plan.getPrincipalAmount()));
        userRepository.save(user);
    }

    @Transactional
    public void processOverdue() {
        List<RepaymentPlan> overduePlans = repaymentPlanRepository
                .findByStatusAndDueDateLessThan(RepaymentStatus.PENDING, LocalDate.now());

        for (RepaymentPlan plan : overduePlans) {
            plan.setStatus(RepaymentStatus.OVERDUE);
            repaymentPlanRepository.save(plan);

            User user = plan.getLoanApplication().getUser();
            user.setOverdueCount(user.getOverdueCount() + 1);
            
            if (user.getOverdueCount() >= 3) {
                user.setBlocked(true);
            }
            
            int scoreReduction = Math.min(user.getCreditScore() - 50, 300);
            user.setCreditScore(Math.max(scoreReduction, 300));
            
            userRepository.save(user);
            
            logger.warn("逾期处理: 用户ID={}, 计划ID={}, 信用分={}", 
                    user.getId(), plan.getId(), user.getCreditScore());
        }
    }

    @Transactional
    public void recoverCreditLimit() {
        List<LoanApplication> completedLoans = loanApplicationRepository.findByStatusIn(
                List.of(LoanApplication.ApplicationStatus.DISBURSED));

        for (LoanApplication loan : completedLoans) {
            List<RepaymentPlan> plans = repaymentPlanRepository.findByLoanApplicationId(loan.getId());
            boolean allPaid = plans.stream()
                    .allMatch(p -> p.getStatus() == RepaymentStatus.PAID || 
                                   p.getStatus() == RepaymentStatus.EARLY_PAID);

            if (allPaid && loan.getStatus() != LoanApplication.ApplicationStatus.COMPLETED) {
                loan.setStatus(LoanApplication.ApplicationStatus.COMPLETED);
                loanApplicationRepository.save(loan);
                
                User user = loan.getUser();
                user.setUsedCreditLimit(user.getUsedCreditLimit().subtract(loan.getApprovedAmount()));
                
                if (user.getOverdueCount() == 0 && user.getCreditScore() < 800) {
                    user.setCreditScore(Math.min(user.getCreditScore() + 10, 800));
                }
                
                userRepository.save(user);
                
                logger.info("额度恢复: 用户ID={}, 恢复额度={}", user.getId(), loan.getApprovedAmount());
            }
        }
    }

    public List<RepaymentPlanResponse> getPlansByUser(Long userId) {
        return repaymentPlanRepository.findByLoanApplicationUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<RepaymentPlanResponse> getPlansByApplication(Long applicationId) {
        return repaymentPlanRepository.findByLoanApplicationId(applicationId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private RepaymentPlanResponse convertToResponse(RepaymentPlan plan) {
        RepaymentPlanResponse response = new RepaymentPlanResponse();
        response.setId(plan.getId());
        response.setLoanApplicationId(plan.getLoanApplication().getId());
        response.setInstallmentNumber(plan.getInstallmentNumber());
        response.setDueDate(plan.getDueDate());
        response.setPrincipalAmount(plan.getPrincipalAmount());
        response.setInterestAmount(plan.getInterestAmount());
        response.setTotalAmount(plan.getTotalAmount());
        response.setStatus(plan.getStatus().name());
        response.setPaidDate(plan.getPaidDate());
        response.setPaidAmount(plan.getPaidAmount());
        return response;
    }
}