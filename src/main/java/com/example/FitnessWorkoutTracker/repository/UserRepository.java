package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Provides database operations for User entities
public interface UserRepository extends JpaRepository<User, Integer> {

    // Finds a user by email address
    Optional<User> findByEmail(String email);
}