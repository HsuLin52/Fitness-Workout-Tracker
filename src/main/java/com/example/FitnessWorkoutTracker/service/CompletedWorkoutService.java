package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.CompletedWorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Contains the business logic for recording completed workouts and calculating calories burned
@Service
public class CompletedWorkoutService {

    private final CompletedWorkoutRepository completedWorkoutRepository;

    public CompletedWorkoutService(CompletedWorkoutRepository completedWorkoutRepository) {
        this.completedWorkoutRepository = completedWorkoutRepository;
    }

    // Return every completed workout stored in the database
    public List<CompletedWorkout> getAllWorkouts() {
        return completedWorkoutRepository.findAll();
    }

    // Return one completed workout by ID
    public Optional<CompletedWorkout> getWorkoutById(Integer id) {
        return completedWorkoutRepository.findById(id);
    }

    // Return all completed workouts recorded by one user
    public List<CompletedWorkout> getWorkoutsByUser(User user) {
        return completedWorkoutRepository.findByUser(user);
    }

    // Return all completed workouts recorded from one workout plan
    public List<CompletedWorkout> getWorkoutsByPlan(WorkoutPlan workoutPlan) {
        return completedWorkoutRepository.findByWorkoutPlan(workoutPlan);
    }

    // Return all completed workouts within an inclusive date range, used by history and dashboard
    public List<CompletedWorkout> getWorkoutsBetween(LocalDate startDate, LocalDate endDate) {
        return completedWorkoutRepository.findByWorkoutDateBetween(startDate, endDate);
    }

    // Return all completed workouts for one user within an inclusive date range
    public List<CompletedWorkout> getWorkoutsByUserBetween(
            User user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return completedWorkoutRepository.findByUserAndWorkoutDateBetween(
                user,
                startDate,
                endDate
        );
    }

    // Calculates calories burned using the agreed formula:
    // Calories Burned = Exercise Calories per Minute x Completed Duration in Minutes
    public double calculateCalories(Exercise exercise, Integer durationMinutes) {
        if (exercise == null || exercise.getCaloriesPerMinute() == null || durationMinutes == null) {
            return 0.0;
        }
        return exercise.getCaloriesPerMinute() * durationMinutes;
    }

    // Saves a new completed workout or updates an existing one, recalculating calories
    // from the exercise's calorie rate and duration so the stored value always matches the formula
    public CompletedWorkout saveWorkout(CompletedWorkout completedWorkout) {
        completedWorkout.setCalories(
                calculateCalories(completedWorkout.getExercise(), completedWorkout.getDuration())
        );
        return completedWorkoutRepository.save(completedWorkout);
    }

    // Delete a completed workout by ID
    public void deleteWorkout(Integer id) {
        completedWorkoutRepository.deleteById(id);
    }
}
