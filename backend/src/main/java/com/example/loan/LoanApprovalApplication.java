package com.example.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApprovalApplication.class, args);
    }
}