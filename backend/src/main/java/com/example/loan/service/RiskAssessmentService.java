package com.example.loan.service;

import com.example.loan.entity.LoanApplication;
import com.example.loan.entity.LoanApplication.RiskLevel;
import com.example.loan.entity.User;
import org.springframework.stereotype.Service;

@Service
public class RiskAssessmentService {

    private static final int HIGH_RISK_CREDIT_SCORE_THRESHOLD = 550;
    private static final int HIGH_RISK_OVERDUE_COUNT = 3;
    private static final BigDecimal HIGH_RISK_LOAN_AMOUNT = new BigDecimal("50000");

    public RiskLevel assessRisk(LoanApplication application) {
        User user = application.getUser();
        int riskScore = 0;

        if (user.getCreditScore() < HIGH_RISK_CREDIT_SCORE_THRESHOLD) {
            riskScore += 30;
        } else if (user.getCreditScore() < 600) {
            riskScore += 15;
        }

        riskScore += user.getOverdueCount() * 10;

        if (application.getRequestedAmount().compareTo(HIGH_RISK_LOAN_AMOUNT) > 0) {
            riskScore += 20;
        }

        if ("D".equals(user.getIncomeLevel())) {
            riskScore += 20;
        } else if ("C".equals(user.getIncomeLevel())) {
            riskScore += 10;
        }

        if (user.getBlocked()) {
            riskScore += 50;
        }

        if (riskScore >= 60) {
            return RiskLevel.HIGH;
        } else if (riskScore >= 30) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    public boolean isHighRisk(LoanApplication application) {
        return assessRisk(application) == RiskLevel.HIGH;
    }
}