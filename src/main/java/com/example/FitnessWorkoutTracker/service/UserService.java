package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

// Contains the business logic for managing application users.
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Returns all users alphabetically by name.
    public List<User> getAllUsers() {
        return userRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                )
        );
    }

    // Checks whether an email address is already used.
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return userRepository.existsByEmailIgnoreCase(
                email.trim()
        );
    }

    // Normalizes and saves a new user.
    public User saveUser(User user) {
        user.setName(user.getName().trim());

        user.setEmail(
                user.getEmail()
                        .trim()
                        .toLowerCase()
        );

        return userRepository.save(user);
    }
}