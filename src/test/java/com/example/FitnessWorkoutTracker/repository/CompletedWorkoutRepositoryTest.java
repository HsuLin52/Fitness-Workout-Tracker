package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// JPA integration test: persists real entities through Hibernate against the project's
// configured MySQL database (replace = NONE, since the project must use MySQL, not an
// embedded test database) and verifies the custom CompletedWorkoutRepository queries.
// Each test runs in a transaction that is rolled back afterwards, so no data is left behind.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompletedWorkoutRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompletedWorkoutRepository completedWorkoutRepository;

    @BeforeEach
    void isolateRepositoryTests() {
    // Temporarily remove existing workouts inside the test transaction
    // so personal/sample database records do not affect test results.
    // Spring rolls the transaction back after each test, so the real
    // workout records are not permanently deleted.
    completedWorkoutRepository.deleteAllInBatch();
    entityManager.flush();
    entityManager.clear();
}

    private User persistUser(String email) {
        User user = new User("Integration Test User", email);
        return entityManager.persistAndFlush(user);
    }

    private Exercise persistExercise(String name, ExerciseType type, double caloriesPerMinute) {
        Exercise exercise = new Exercise(name, type, caloriesPerMinute);
        return entityManager.persistAndFlush(exercise);
    }

    private CompletedWorkout completedWorkout(User user, Exercise exercise, LocalDate date, int duration) {
        CompletedWorkout workout = new CompletedWorkout();
        workout.setUser(user);
        workout.setExercise(exercise);
        workout.setWorkoutDate(date);
        workout.setDuration(duration);
        workout.setCalories(exercise.getCaloriesPerMinute() * duration);
        return workout;
    }

    @Test
    void findByUserReturnsOnlyWorkoutsPersistedForThatUser() {
        User owner = persistUser("integration.owner@example.com");
        User otherUser = persistUser("integration.other@example.com");
        Exercise exercise = persistExercise("Rowing", ExerciseType.CARDIO, 9.0);

        CompletedWorkout ownWorkout = completedWorkout(owner, exercise, LocalDate.now(), 20);
        entityManager.persistAndFlush(ownWorkout);

        CompletedWorkout otherWorkout = completedWorkout(otherUser, exercise, LocalDate.now(), 20);
        entityManager.persistAndFlush(otherWorkout);

        List<CompletedWorkout> found = completedWorkoutRepository.findByUser(owner);

        assertEquals(1, found.size());
        assertEquals(ownWorkout.getId(), found.get(0).getId());
    }

    @Test
    void findByWorkoutDateBetweenReturnsOnlyWorkoutsWithinRange() {
        User user = persistUser("integration.range@example.com");
        Exercise exercise = persistExercise("Swimming", ExerciseType.CARDIO, 7.0);

        CompletedWorkout inRange = completedWorkout(user, exercise, LocalDate.now().minusDays(2), 15);
        entityManager.persistAndFlush(inRange);

        CompletedWorkout outOfRange = completedWorkout(user, exercise, LocalDate.now().minusDays(30), 15);
        entityManager.persistAndFlush(outOfRange);

        List<CompletedWorkout> found = completedWorkoutRepository.findByWorkoutDateBetween(
                LocalDate.now().minusDays(7),
                LocalDate.now()
        );

        assertEquals(1, found.size());
        assertEquals(inRange.getId(), found.get(0).getId());
    }

    @Test
    void findByExerciseTypeReturnsOnlyMatchingExerciseType() {
        User user = persistUser("integration.type@example.com");
        Exercise cardioExercise = persistExercise("Cycling", ExerciseType.CARDIO, 8.0);
        Exercise strengthExercise = persistExercise("Bench Press", ExerciseType.STRENGTH, 5.0);

        CompletedWorkout cardioWorkout = completedWorkout(user, cardioExercise, LocalDate.now(), 30);
        entityManager.persistAndFlush(cardioWorkout);

        CompletedWorkout strengthWorkout = completedWorkout(user, strengthExercise, LocalDate.now(), 30);
        entityManager.persistAndFlush(strengthWorkout);

        List<CompletedWorkout> found = completedWorkoutRepository.findByExercise_Type(ExerciseType.CARDIO);

        assertEquals(1, found.size());
        assertEquals(cardioWorkout.getId(), found.get(0).getId());
    }

    @Test
    void savedCalorieValueMatchesTheAgreedFormula() {
        User user = persistUser("integration.calories@example.com");
        Exercise exercise = persistExercise("Running", ExerciseType.CARDIO, 10.0);

        CompletedWorkout workout = completedWorkout(user, exercise, LocalDate.now(), 25);
        CompletedWorkout saved = entityManager.persistFlushFind(workout);

        assertEquals(250.0, saved.getCalories());
    }
}
