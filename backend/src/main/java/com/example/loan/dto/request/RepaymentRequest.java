package com.example.loan.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class RepaymentRequest {

    @NotNull(message = "还款计划ID不能为空")
    private Long planId;

    @NotNull(message = "还款金额不能为空")
    @Positive(message = "还款金额必须为正数")
    private BigDecimal amount;

    private Boolean earlyRepayment;

    public RepaymentRequest() {
        this.earlyRepayment = false;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getEarlyRepayment() {
        return earlyRepayment;
    }

    public void setEarlyRepayment(Boolean earlyRepayment) {
        this.earlyRepayment = earlyRepayment;
    }
}