package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Contains the business logic for managing exercises
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    // Constructor
    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    // Method that returns every exercise stored in the database
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    // Method that returns one exercise by ID
    public Optional<Exercise> getExerciseById(Integer id) {
        return exerciseRepository.findById(id);
    }

    // Method that saves a new exercise or updates an existing exercise
    public Exercise saveExercise(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    // Method that deletes an exercise by ID
    public void deleteExercise(Integer id) {
        exerciseRepository.deleteById(id);
    }

    // Method that searches exercises by name.
    public List<Exercise> searchByName(String name) {
        return exerciseRepository.findByNameContainingIgnoreCase(name);
    }

    // Method that searches exercises by type
    public List<Exercise> searchByType(ExerciseType type) {
        return exerciseRepository.findByType(type);
    }

    // Method that searches exercises using both name and type
    public List<Exercise> searchByNameAndType(
            String name,
            ExerciseType type
    ) {
        return exerciseRepository.findByNameContainingIgnoreCaseAndType(name, type);
    }
}