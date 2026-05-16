package com.example.loan.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "申请金额不能为空")
    @Positive(message = "申请金额必须为正数")
    private BigDecimal requestedAmount;

    public LoanApplicationRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
}