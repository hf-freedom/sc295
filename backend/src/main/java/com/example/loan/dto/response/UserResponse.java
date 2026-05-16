package com.example.loan.dto.response;

import java.math.BigDecimal;

public class UserResponse {

    private Long id;
    private String name;
    private String phone;
    private Integer creditScore;
    private String incomeLevel;
    private BigDecimal totalCreditLimit;
    private BigDecimal usedCreditLimit;
    private BigDecimal availableCreditLimit;
    private Boolean blocked;
    private Integer overdueCount;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getIncomeLevel() {
        return incomeLevel;
    }

    public void setIncomeLevel(String incomeLevel) {
        this.incomeLevel = incomeLevel;
    }

    public BigDecimal getTotalCreditLimit() {
        return totalCreditLimit;
    }

    public void setTotalCreditLimit(BigDecimal totalCreditLimit) {
        this.totalCreditLimit = totalCreditLimit;
    }

    public BigDecimal getUsedCreditLimit() {
        return usedCreditLimit;
    }

    public void setUsedCreditLimit(BigDecimal usedCreditLimit) {
        this.usedCreditLimit = usedCreditLimit;
    }

    public BigDecimal getAvailableCreditLimit() {
        return availableCreditLimit;
    }

    public void setAvailableCreditLimit(BigDecimal availableCreditLimit) {
        this.availableCreditLimit = availableCreditLimit;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }
}