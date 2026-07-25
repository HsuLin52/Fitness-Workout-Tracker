package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Provides database operations and search methods for exercises
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

    // Finds exercises whose names contain the given text ignoring uppercase and lowercase differences
    List<Exercise> findByNameContainingIgnoreCase(String name);

    // Finds all exercises belonging to a specific exercise type
    List<Exercise> findByType(ExerciseType type);

    // Searches by both name and exercise type
    List<Exercise> findByNameContainingIgnoreCaseAndType(
            String name,
            ExerciseType type
    );
}