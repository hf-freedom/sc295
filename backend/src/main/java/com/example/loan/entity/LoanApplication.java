package com.example.loan.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal requestedAmount;

    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String rejectionReason;

    private Boolean highRisk;

    private Boolean manualReviewed;

    private LocalDate applicationDate;

    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    public LoanApplication() {
        this.status = ApplicationStatus.PENDING;
        this.highRisk = false;
        this.manualReviewed = false;
        this.applicationDate = LocalDate.now();
    }

    public enum ApplicationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        DISBURSED,
        COMPLETED
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Boolean getHighRisk() {
        return highRisk;
    }

    public void setHighRisk(Boolean highRisk) {
        this.highRisk = highRisk;
    }

    public Boolean getManualReviewed() {
        return manualReviewed;
    }

    public void setManualReviewed(Boolean manualReviewed) {
        this.manualReviewed = manualReviewed;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
}