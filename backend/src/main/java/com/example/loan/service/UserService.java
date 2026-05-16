package com.example.loan.service;

import com.example.loan.dto.response.UserResponse;
import com.example.loan.entity.User;
import com.example.loan.exception.BusinessException;
import com.example.loan.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditLimitCalculator creditLimitCalculator;

    @Transactional
    public UserResponse createUser(String name, String phone, Integer creditScore, String incomeLevel) {
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setCreditScore(creditScore != null ? creditScore : 600);
        user.setIncomeLevel(incomeLevel != null ? incomeLevel : "C");
        user.setTotalCreditLimit(creditLimitCalculator.calculateCreditLimit(user));
        
        User saved = userRepository.save(user);
        logger.info("用户创建成功: {}", saved.getId());
        
        return convertToResponse(saved);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long userId, String name, String phone, String incomeLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (name != null) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (incomeLevel != null) {
            user.setIncomeLevel(incomeLevel);
            user.setTotalCreditLimit(creditLimitCalculator.calculateCreditLimit(user));
        }
        
        User saved = userRepository.save(user);
        logger.info("用户更新成功: {}", userId);
        
        return convertToResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userRepository.delete(user);
        logger.info("用户删除成功: {}", userId);
    }

    public BigDecimal calculateAvailableCredit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return user.getAvailableCreditLimit();
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setCreditScore(user.getCreditScore());
        response.setIncomeLevel(user.getIncomeLevel());
        response.setTotalCreditLimit(user.getTotalCreditLimit());
        response.setUsedCreditLimit(user.getUsedCreditLimit());
        response.setAvailableCreditLimit(user.getAvailableCreditLimit());
        response.setBlocked(user.getBlocked());
        response.setOverdueCount(user.getOverdueCount());
        return response;
    }
}