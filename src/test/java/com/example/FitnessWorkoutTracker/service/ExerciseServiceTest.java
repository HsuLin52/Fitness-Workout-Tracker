package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void getAllExercises_ShouldReturnExercises() {

        Exercise exercise = new Exercise();

        when(exerciseRepository.findAll())
                .thenReturn(List.of(exercise));

        List<Exercise> result = exerciseService.getAllExercises();

        assertEquals(1, result.size());
        verify(exerciseRepository).findAll();
    }

    @Test
    void getExerciseById_ShouldReturnExercise() {

        Exercise exercise = new Exercise();
        exercise.setId(1);

        when(exerciseRepository.findById(1))
                .thenReturn(Optional.of(exercise));

        Optional<Exercise> result =
                exerciseService.getExerciseById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());

        verify(exerciseRepository).findById(1);
    }

    @Test
    void saveExercise_ShouldSaveExercise() {

        Exercise exercise = new Exercise();

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        Exercise result =
                exerciseService.saveExercise(exercise);

        assertNotNull(result);

        verify(exerciseRepository).save(exercise);
    }

    @Test
    void deleteExercise_ShouldDeleteExercise() {

        exerciseService.deleteExercise(3);

        verify(exerciseRepository).deleteById(3);
    }

    @Test
    void searchByName_ShouldReturnExercises() {

        Exercise exercise = new Exercise();

        when(exerciseRepository.findByNameContainingIgnoreCase("Push"))
                .thenReturn(List.of(exercise));

        List<Exercise> result =
                exerciseService.searchByName("Push");

        assertEquals(1, result.size());

        verify(exerciseRepository)
                .findByNameContainingIgnoreCase("Push");
    }

    @Test
    void searchByType_ShouldReturnExercises() {

        Exercise exercise = new Exercise();

        when(exerciseRepository.findByType(ExerciseType.STRENGTH))
                .thenReturn(List.of(exercise));

        List<Exercise> result =
                exerciseService.searchByType(ExerciseType.STRENGTH);

        assertEquals(1, result.size());

        verify(exerciseRepository)
                .findByType(ExerciseType.STRENGTH);
    }

    @Test
    void searchByNameAndType_ShouldReturnExercises() {

        Exercise exercise = new Exercise();

        when(exerciseRepository.findByNameContainingIgnoreCaseAndType(
                "Push",
                ExerciseType.STRENGTH))
                .thenReturn(List.of(exercise));

        List<Exercise> result =
                exerciseService.searchByNameAndType(
                        "Push",
                        ExerciseType.STRENGTH);

        assertEquals(1, result.size());

        verify(exerciseRepository)
                .findByNameContainingIgnoreCaseAndType(
                        "Push",
                        ExerciseType.STRENGTH);
    }
}