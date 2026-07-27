package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

// Provides database operations and shared query methods for completed workouts.
// Date-range and exercise-type queries here are used by the history (Member 4)
// and dashboard (Member 5) modules.
public interface CompletedWorkoutRepository extends JpaRepository<CompletedWorkout, Integer> {

    // All workouts recorded by one user
    List<CompletedWorkout> findByUser(User user);

    // All workouts recorded from one workout plan
    List<CompletedWorkout> findByWorkoutPlan(WorkoutPlan workoutPlan);

    // All workouts within an inclusive date range, used for weekly/monthly summaries
    List<CompletedWorkout> findByWorkoutDateBetween(LocalDate startDate, LocalDate endDate);

    // All workouts for one user within an inclusive date range
    List<CompletedWorkout> findByUserAndWorkoutDateBetween(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // All workouts whose exercise belongs to the given exercise type
    List<CompletedWorkout> findByExercise_Type(ExerciseType type);
}
