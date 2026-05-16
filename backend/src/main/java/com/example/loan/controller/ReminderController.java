package com.example.loan.controller;

import com.example.loan.entity.Reminder;
import com.example.loan.entity.Reminder.ReminderType;
import com.example.loan.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderRepository reminderRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllReminders() {
        List<Map<String, Object>> reminders = reminderRepository.findAll().stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/unsent")
    public ResponseEntity<List<Map<String, Object>>> getUnsentReminders() {
        List<Map<String, Object>> reminders = reminderRepository.findBySent(false).stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Map<String, Object>>> getRemindersByType(@PathVariable String type) {
        ReminderType reminderType = ReminderType.valueOf(type.toUpperCase());
        List<Map<String, Object>> reminders = reminderRepository.findByType(reminderType).stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reminders);
    }

    @PutMapping("/{reminderId}/mark-sent")
    public ResponseEntity<Map<String, Object>> markAsSent(@PathVariable Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder == null) {
            return ResponseEntity.notFound().build();
        }
        reminder.setSent(true);
        reminder.setReminderDate(LocalDate.now());
        Reminder saved = reminderRepository.save(reminder);
        return ResponseEntity.ok(convertToMap(saved));
    }

    private Map<String, Object> convertToMap(Reminder reminder) {
        return Map.of(
                "id", reminder.getId(),
                "planId", reminder.getRepaymentPlan().getId(),
                "type", reminder.getType().name(),
                "reminderDate", reminder.getReminderDate(),
                "sent", reminder.getSent(),
                "message", reminder.getMessage()
        );
    }
}