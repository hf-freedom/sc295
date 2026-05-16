package com.example.loan.service;

import com.example.loan.dto.request.LoanApplicationRequest;
import com.example.loan.dto.response.LoanApplicationResponse;
import com.example.loan.entity.LoanApplication;
import com.example.loan.entity.LoanApplication.ApplicationStatus;
import com.example.loan.entity.LoanApplication.RiskLevel;
import com.example.loan.entity.User;
import com.example.loan.exception.BusinessException;
import com.example.loan.repository.LoanApplicationRepository;
import com.example.loan.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private CreditLimitCalculator creditLimitCalculator;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Autowired
    private RepaymentService repaymentService;

    @Transactional
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getBlocked()) {
            throw new BusinessException("用户已被限制借款");
        }

        BigDecimal calculatedLimit = creditLimitCalculator.calculateCreditLimit(user);
        
        if (request.getRequestedAmount().compareTo(calculatedLimit) > 0) {
            throw new BusinessException("申请金额超过可用额度");
        }

        LoanApplication application = new LoanApplication();
        application.setUser(user);
        application.setRequestedAmount(request.getRequestedAmount());
        application.setStatus(ApplicationStatus.PENDING);

        RiskLevel riskLevel = riskAssessmentService.assessRisk(application);
        application.setRiskLevel(riskLevel);
        application.setHighRisk(riskLevel == RiskLevel.HIGH);

        LoanApplication saved = loanApplicationRepository.save(application);
        logger.info("借款申请创建成功: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Transactional
    public LoanApplicationResponse reviewApplication(Long applicationId, boolean approve, String reason) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.PENDING && 
            application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BusinessException("申请状态不允许审核");
        }

        application.setStatus(approve ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        application.setReviewDate(LocalDate.now());
        application.setManualReviewed(true);

        if (approve) {
            application.setApprovedAmount(application.getRequestedAmount());
            User user = application.getUser();
            user.setUsedCreditLimit(user.getUsedCreditLimit().add(application.getRequestedAmount()));
            userRepository.save(user);
            logger.info("借款申请通过: {}", applicationId);
        } else {
            application.setRejectionReason(reason);
            logger.info("借款申请拒绝: {}, 原因: {}", applicationId, reason);
        }

        LoanApplication saved = loanApplicationRepository.save(application);
        return convertToResponse(saved);
    }

    @Transactional
    public LoanApplicationResponse disburseLoan(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("申请未通过审核，无法放款");
        }

        application.setStatus(ApplicationStatus.DISBURSED);
        LoanApplication saved = loanApplicationRepository.save(application);

        repaymentService.generateRepaymentPlan(saved);
        logger.info("放款成功: {}", applicationId);

        return convertToResponse(saved);
    }

    @Transactional
    public void autoReviewApplications() {
        List<LoanApplication> pendingApplications = loanApplicationRepository.findByStatus(ApplicationStatus.PENDING);

        for (LoanApplication application : pendingApplications) {
            if (application.getHighRisk() && !application.getManualReviewed()) {
                application.setStatus(ApplicationStatus.UNDER_REVIEW);
                loanApplicationRepository.save(application);
                logger.info("高风险申请进入人工复核队列: {}", application.getId());
            } else {
                RiskLevel riskLevel = application.getRiskLevel();
                if (riskLevel == RiskLevel.LOW) {
                    reviewApplication(application.getId(), true, "自动审核通过");
                } else {
                    reviewApplication(application.getId(), false, "风险等级过高");
                }
            }
        }
    }

    public List<LoanApplicationResponse> getApplicationsByUser(Long userId) {
        return loanApplicationRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponse> getAllApplications() {
        return loanApplicationRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponse> getHighRiskApplications() {
        return loanApplicationRepository.findByHighRiskAndManualReviewed(true, false)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private LoanApplicationResponse convertToResponse(LoanApplication application) {
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setId(application.getId());
        response.setUserId(application.getUser().getId());
        response.setUserName(application.getUser().getName());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setApprovedAmount(application.getApprovedAmount());
        response.setStatus(application.getStatus().name());
        response.setRejectionReason(application.getRejectionReason());
        response.setHighRisk(application.getHighRisk());
        response.setManualReviewed(application.getManualReviewed());
        response.setApplicationDate(application.getApplicationDate());
        response.setReviewDate(application.getReviewDate());
        response.setRiskLevel(application.getRiskLevel() != null ? application.getRiskLevel().name() : null);
        return response;
    }
}