package com.example.loan.repository;

import com.example.loan.entity.Reminder;
import com.example.loan.entity.Reminder.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findBySent(Boolean sent);
    List<Reminder> findByType(ReminderType type);
    List<Reminder> findByRepaymentPlanId(Long planId);
}