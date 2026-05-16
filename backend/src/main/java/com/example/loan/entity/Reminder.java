package com.example.loan.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "repayment_plan_id")
    private RepaymentPlan repaymentPlan;

    @Enumerated(EnumType.STRING)
    private ReminderType type;

    private LocalDate reminderDate;

    private Boolean sent;

    private String message;

    public Reminder() {
        this.sent = false;
        this.reminderDate = LocalDate.now();
    }

    public enum ReminderType {
        DUE_SOON,
        OVERDUE,
        RECOVERY
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepaymentPlan getRepaymentPlan() {
        return repaymentPlan;
    }

    public void setRepaymentPlan(RepaymentPlan repaymentPlan) {
        this.repaymentPlan = repaymentPlan;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}