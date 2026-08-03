package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

// Provides database operations and shared query methods for completed workouts.
// Date-range and exercise-type queries here are used by the history (Member 4)
// and dashboard (Member 5) modules.
public interface CompletedWorkoutRepository
        extends JpaRepository<CompletedWorkout, Integer> {

    // All workouts recorded by one user
    List<CompletedWorkout> findByUser(User user);

    // All workouts recorded from one workout plan
    List<CompletedWorkout> findByWorkoutPlan(WorkoutPlan workoutPlan);

    // All workouts within an inclusive date range
    List<CompletedWorkout> findByWorkoutDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // All workouts for one user within an inclusive date range
    List<CompletedWorkout> findByUserAndWorkoutDateBetween(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // All workouts whose exercise belongs to the given exercise type
    List<CompletedWorkout> findByExercise_Type(ExerciseType type);

    // Search and filter query for the workout-history page.
    // Pageable handles pagination and sorting.
    @Query(
            value = """
                    SELECT cw
                    FROM CompletedWorkout cw
                    JOIN FETCH cw.exercise exercise
                    JOIN FETCH cw.user workoutUser
                    LEFT JOIN FETCH cw.workoutPlan plan
                    WHERE cw.completed = true
                      AND (
                          :search IS NULL
                          OR LOWER(exercise.name)
                             LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                      AND (
                          :type IS NULL
                          OR exercise.type = :type
                      )
                      AND (
                          :startDate IS NULL
                          OR cw.workoutDate >= :startDate
                      )
                      AND (
                          :endDate IS NULL
                          OR cw.workoutDate <= :endDate
                      )
                      AND (
                          :minCalories IS NULL
                          OR cw.calories >= :minCalories
                      )
                      AND (
                          :planId IS NULL
                          OR plan.id = :planId
                      )
                    """,
            countQuery = """
                    SELECT COUNT(cw)
                    FROM CompletedWorkout cw
                    JOIN cw.exercise exercise
                    LEFT JOIN cw.workoutPlan plan
                    WHERE cw.completed = true
                      AND (
                          :search IS NULL
                          OR LOWER(exercise.name)
                             LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                      AND (
                          :type IS NULL
                          OR exercise.type = :type
                      )
                      AND (
                          :startDate IS NULL
                          OR cw.workoutDate >= :startDate
                      )
                      AND (
                          :endDate IS NULL
                          OR cw.workoutDate <= :endDate
                      )
                      AND (
                          :minCalories IS NULL
                          OR cw.calories >= :minCalories
                      )
                      AND (
                          :planId IS NULL
                          OR plan.id = :planId
                      )
                    """
    )
    Page<CompletedWorkout> searchHistory(
            @Param("search") String search,
            @Param("type") ExerciseType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minCalories") Double minCalories,
            @Param("planId") Integer planId,
            Pageable pageable
    );
}