package com.example.victorymoments.repository;

import com.example.victorymoments.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(String email, String phoneNumber);
}