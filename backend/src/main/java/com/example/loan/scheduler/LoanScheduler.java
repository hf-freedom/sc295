package com.example.loan.scheduler;

import com.example.loan.entity.RepaymentPlan;
import com.example.loan.entity.RepaymentPlan.RepaymentStatus;
import com.example.loan.entity.Reminder;
import com.example.loan.entity.Reminder.ReminderType;
import com.example.loan.repository.RepaymentPlanRepository;
import com.example.loan.repository.ReminderRepository;
import com.example.loan.service.LoanService;
import com.example.loan.service.RepaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LoanScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LoanScheduler.class);

    @Autowired
    private RepaymentPlanRepository repaymentPlanRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private RepaymentService repaymentService;

    @Autowired
    private LoanService loanService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDueReminders() {
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        
        List<RepaymentPlan> upcomingPlans = repaymentPlanRepository
                .findByDueDateBetween(LocalDate.now(), threeDaysLater)
                .stream()
                .filter(p -> p.getStatus() == RepaymentStatus.PENDING)
                .toList();

        for (RepaymentPlan plan : upcomingPlans) {
            Reminder reminder = new Reminder();
            reminder.setRepaymentPlan(plan);
            reminder.setType(ReminderType.DUE_SOON);
            reminder.setMessage(String.format("您的还款计划将于%s到期，金额：%s", 
                    plan.getDueDate(), plan.getTotalAmount()));
            reminder.setReminderDate(LocalDate.now());
            reminder.setSent(false);
            
            reminderRepository.save(reminder);
        }
        
        logger.info("到期提醒生成完成，共{}条", upcomingPlans.size());
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void processOverdueRecords() {
        repaymentService.processOverdue();
        logger.info("逾期记录处理完成");
    }

    @Scheduled(cron = "0 0 11 * * ?")
    public void recoverCreditLimits() {
        repaymentService.recoverCreditLimit();
        logger.info("额度恢复处理完成");
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void autoReviewApplications() {
        loanService.autoReviewApplications();
        logger.info("自动审核完成");
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void generateOverdueReminders() {
        List<RepaymentPlan> overduePlans = repaymentPlanRepository
                .findByStatus(RepaymentStatus.OVERDUE);

        for (RepaymentPlan plan : overduePlans) {
            Reminder existingReminder = reminderRepository
                    .findByRepaymentPlanId(plan.getId())
                    .stream()
                    .filter(r -> r.getType() == ReminderType.OVERDUE)
                    .findFirst()
                    .orElse(null);

            if (existingReminder == null) {
                Reminder reminder = new Reminder();
                reminder.setRepaymentPlan(plan);
                reminder.setType(ReminderType.OVERDUE);
                reminder.setMessage(String.format("您的还款计划已逾期，请尽快还款，金额：%s", 
                        plan.getTotalAmount()));
                reminder.setReminderDate(LocalDate.now());
                reminder.setSent(false);
                
                reminderRepository.save(reminder);
            }
        }
        
        logger.info("逾期提醒生成完成");
    }
}