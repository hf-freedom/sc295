package com.example.loan.controller;

import com.example.loan.dto.response.UserResponse;
import com.example.loan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        Integer creditScore = request.get("creditScore") != null ? ((Number) request.get("creditScore")).intValue() : null;
        String incomeLevel = (String) request.get("incomeLevel");
        
        UserResponse response = userService.createUser(name, phone, creditScore, incomeLevel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String incomeLevel = (String) request.get("incomeLevel");
        
        UserResponse response = userService.updateUser(userId, name, phone, incomeLevel);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/credit-limit")
    public ResponseEntity<Map<String, Object>> getCreditLimit(@PathVariable Long userId) {
        BigDecimal limit = userService.calculateAvailableCredit(userId);
        return ResponseEntity.ok(Map.of("availableCreditLimit", limit));
    }
}