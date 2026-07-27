package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.repository.CompletedWorkoutRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests saving, editing, deleting, and the calorie calculation/validation rules on CompletedWorkout
@ExtendWith(MockitoExtension.class)
class CompletedWorkoutServiceTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private CompletedWorkoutRepository completedWorkoutRepository;

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
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);
        double calories = service.calculateCalories(exercise(8.0), 45);
        assertEquals(360.0, calories);
    }

    @Test
    void calculateCaloriesReturnsZeroWhenExerciseIsNull() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);
        double calories = service.calculateCalories(null, 30);
        assertEquals(0.0, calories);
    }

    @Test
    void calculateCaloriesReturnsZeroWhenDurationIsNull() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);
        double calories = service.calculateCalories(exercise(8.0), null);
        assertEquals(0.0, calories);
    }

    @Test
    void saveWorkoutCalculatesAndPersistsCalories() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);

        CompletedWorkout workout = validWorkout();
        workout.setExercise(exercise(8.0));
        workout.setDuration(45);

        when(completedWorkoutRepository.save(any(CompletedWorkout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompletedWorkout saved = service.saveWorkout(workout);

        ArgumentCaptor<CompletedWorkout> captor = ArgumentCaptor.forClass(CompletedWorkout.class);
        verify(completedWorkoutRepository).save(captor.capture());

        assertEquals(360.0, captor.getValue().getCalories());
        assertEquals(360.0, saved.getCalories());
    }

    @Test
    void savingAnEditedWorkoutRecalculatesCaloriesFromTheNewExerciseAndDuration() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);

        // Simulate an existing workout being edited: exercise and duration both change
        CompletedWorkout workout = validWorkout();
        workout.setId(7);
        workout.setExercise(exercise(5.0));
        workout.setDuration(20);
        workout.setCalories(100.0); // stale value from before the edit

        when(completedWorkoutRepository.save(any(CompletedWorkout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompletedWorkout saved = service.saveWorkout(workout);

        assertEquals(100.0, saved.getCalories());
        assertEquals(7, saved.getId());
    }

    @Test
    void deleteWorkoutDelegatesToRepository() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);

        service.deleteWorkout(3);

        verify(completedWorkoutRepository).deleteById(3);
    }

    @Test
    void getWorkoutByIdReturnsRepositoryResult() {
        CompletedWorkoutService service = new CompletedWorkoutService(completedWorkoutRepository);
        CompletedWorkout workout = validWorkout();
        workout.setId(9);

        when(completedWorkoutRepository.findById(9)).thenReturn(Optional.of(workout));

        Optional<CompletedWorkout> found = service.getWorkoutById(9);

        assertTrue(found.isPresent());
        assertEquals(9, found.get().getId());
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
    void zeroDurationIsRejected() {
        CompletedWorkout workout = validWorkout();
        workout.setDuration(0);

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
