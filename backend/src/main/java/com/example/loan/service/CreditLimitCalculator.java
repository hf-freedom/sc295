package com.example.loan.service;

import com.example.loan.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CreditLimitCalculator {

    private static final BigDecimal BASE_LIMIT = new BigDecimal("10000");
    private static final BigDecimal CREDIT_SCORE_FACTOR = new BigDecimal("100");
    private static final BigDecimal INCOME_MULTIPLIER_A = new BigDecimal("5");
    private static final BigDecimal INCOME_MULTIPLIER_B = new BigDecimal("3");
    private static final BigDecimal INCOME_MULTIPLIER_C = new BigDecimal("2");
    private static final BigDecimal INCOME_MULTIPLIER_D = new BigDecimal("1");

    public BigDecimal calculateCreditLimit(User user) {
        BigDecimal creditScoreFactor = calculateCreditScoreFactor(user.getCreditScore());
        BigDecimal incomeFactor = calculateIncomeFactor(user.getIncomeLevel());
        BigDecimal repaymentHistoryFactor = calculateRepaymentHistoryFactor(user.getOverdueCount());

        BigDecimal limit = BASE_LIMIT
                .multiply(creditScoreFactor)
                .multiply(incomeFactor)
                .multiply(repaymentHistoryFactor);

        return limit.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCreditScoreFactor(Integer creditScore) {
        if (creditScore >= 800) {
            return new BigDecimal("2.0");
        } else if (creditScore >= 700) {
            return new BigDecimal("1.5");
        } else if (creditScore >= 600) {
            return new BigDecimal("1.0");
        } else if (creditScore >= 500) {
            return new BigDecimal("0.6");
        } else {
            return new BigDecimal("0.3");
        }
    }

    private BigDecimal calculateIncomeFactor(String incomeLevel) {
        switch (incomeLevel) {
            case "A":
                return INCOME_MULTIPLIER_A;
            case "B":
                return INCOME_MULTIPLIER_B;
            case "C":
                return INCOME_MULTIPLIER_C;
            default:
                return INCOME_MULTIPLIER_D;
        }
    }

    private BigDecimal calculateRepaymentHistoryFactor(Integer overdueCount) {
        if (overdueCount == 0) {
            return new BigDecimal("1.0");
        } else if (overdueCount == 1) {
            return new BigDecimal("0.8");
        } else if (overdueCount == 2) {
            return new BigDecimal("0.6");
        } else {
            return new BigDecimal("0.3");
        }
    }
}