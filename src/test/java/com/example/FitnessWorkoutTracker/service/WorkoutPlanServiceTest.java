package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.WorkoutPlanRepository;
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
class WorkoutPlanServiceTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    @Test
    void getAllPlans_ShouldReturnAllPlans() {

        WorkoutPlan plan = new WorkoutPlan();

        when(workoutPlanRepository.findAll())
                .thenReturn(List.of(plan));

        List<WorkoutPlan> result = workoutPlanService.getAllPlans();

        assertEquals(1, result.size());
        verify(workoutPlanRepository).findAll();
    }

    @Test
    void getPlanById_ShouldReturnPlan() {

        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1);

        when(workoutPlanRepository.findById(1))
                .thenReturn(Optional.of(plan));

        Optional<WorkoutPlan> result =
                workoutPlanService.getPlanById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());

        verify(workoutPlanRepository).findById(1);
    }

    @Test
    void savePlan_ShouldSavePlan() {

        WorkoutPlan plan = new WorkoutPlan();

        when(workoutPlanRepository.save(plan))
                .thenReturn(plan);

        WorkoutPlan result =
                workoutPlanService.savePlan(plan);

        assertNotNull(result);

        verify(workoutPlanRepository).save(plan);
    }

    @Test
    void deletePlan_ShouldDeletePlan() {

        workoutPlanService.deletePlan(5);

        verify(workoutPlanRepository).deleteById(5);
    }

    @Test
    void getPlansByUser_ShouldReturnPlans() {

        User user = new User();

        WorkoutPlan plan = new WorkoutPlan();

        when(workoutPlanRepository.findByUser(user))
                .thenReturn(List.of(plan));

        List<WorkoutPlan> result =
                workoutPlanService.getPlansByUser(user);

        assertEquals(1, result.size());

        verify(workoutPlanRepository).findByUser(user);
    }
}