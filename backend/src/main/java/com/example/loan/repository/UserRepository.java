package com.example.loan.repository;

import com.example.loan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByBlocked(Boolean blocked);
    List<User> findByCreditScoreLessThan(Integer creditScore);
}