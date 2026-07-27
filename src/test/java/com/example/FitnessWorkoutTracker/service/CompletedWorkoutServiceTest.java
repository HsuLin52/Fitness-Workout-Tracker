package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests the calorie calculation formula and the validation rules on CompletedWorkout
class CompletedWorkoutServiceTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private final CompletedWorkoutService completedWorkoutService = new CompletedWorkoutService(null);

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private Exercise exercise(double caloriesPerMinute) {
        return new Exercise("Running", ExerciseType.CARDIO, caloriesPerMinute);
    }

    private CompletedWorkout validWorkout() {
        CompletedWorkout workout = new CompletedWorkout();
        workout.setExercise(exercise(10.0));
        workout.setWorkoutDate(LocalDate.now());
        workout.setDuration(30);
        workout.setSets(3);
        workout.setReps(10);
        workout.setNotes("Felt good");
        workout.setCompleted(true);
        return workout;
    }

    // Calories Burned = Exercise Calories per Minute x Completed Duration in Minutes
    @Test
    void calculateCaloriesMultipliesRateByDuration() {
        double calories = completedWorkoutService.calculateCalories(exercise(8.0), 45);
        assertEquals(360.0, calories);
    }

    @Test
    void calculateCaloriesReturnsZeroWhenExerciseIsNull() {
        double calories = completedWorkoutService.calculateCalories(null, 30);
        assertEquals(0.0, calories);
    }

    @Test
    void calculateCaloriesReturnsZeroWhenDurationIsNull() {
        double calories = completedWorkoutService.calculateCalories(exercise(8.0), null);
        assertEquals(0.0, calories);
    }

    @Test
    void calculateCaloriesHandlesZeroDuration() {
        double calories = completedWorkoutService.calculateCalories(exercise(8.0), 0);
        assertEquals(0.0, calories);
    }

    @Test
    void validWorkoutHasNoViolations() {
        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(validWorkout());
        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeDurationIsRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setDuration(-5);

        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(workout);

        assertFalse(violations.isEmpty());
    }

    @Test
    void negativeSetsAndRepsAreRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setSets(-1);
        workout.setReps(-2);

        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(workout);

        assertEquals(2, violations.size());
    }

    @Test
    void negativeCaloriesIsRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setCalories(-100.0);

        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(workout);

        assertFalse(violations.isEmpty());
    }

    @Test
    void futureWorkoutDateIsRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setWorkoutDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(workout);

        assertFalse(violations.isEmpty());
    }

    @Test
    void missingDurationIsRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setDuration(null);

        Set<ConstraintViolation<CompletedWorkout>> violations = validator.validate(workout);

        assertFalse(violations.isEmpty());
    }
}
