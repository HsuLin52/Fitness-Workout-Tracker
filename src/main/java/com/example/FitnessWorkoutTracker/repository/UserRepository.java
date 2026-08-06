package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Provides database operations for User entities.
public interface UserRepository extends JpaRepository<User, Integer> {

    // Finds one user by email address.
    Optional<User> findByEmail(String email);

    // Checks for an existing email without being case-sensitive.
    // This prevents duplicate users such as:
    // person@example.com and PERSON@example.com
    boolean existsByEmailIgnoreCase(String email);
}