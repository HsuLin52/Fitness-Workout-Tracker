package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import com.example.FitnessWorkoutTracker.repository.WorkoutPlanItemRepository;
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
class WorkoutPlanItemServiceTest {

    @Mock
    private WorkoutPlanItemRepository workoutPlanItemRepository;

    @InjectMocks
    private WorkoutPlanItemService workoutPlanItemService;

    @Test
    void getItemsByPlan_ShouldReturnItems() {

        WorkoutPlan plan = new WorkoutPlan();
        WorkoutPlanItem item = new WorkoutPlanItem();

        when(workoutPlanItemRepository.findByWorkoutPlan(plan))
                .thenReturn(List.of(item));

        List<WorkoutPlanItem> result =
                workoutPlanItemService.getItemsByPlan(plan);

        assertEquals(1, result.size());
        verify(workoutPlanItemRepository).findByWorkoutPlan(plan);
    }

    @Test
    void getItemById_ShouldReturnItem() {

        WorkoutPlanItem item = new WorkoutPlanItem();
        item.setId(1);

        when(workoutPlanItemRepository.findById(1))
                .thenReturn(Optional.of(item));

        Optional<WorkoutPlanItem> result =
                workoutPlanItemService.getItemById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());

        verify(workoutPlanItemRepository).findById(1);
    }

    @Test
    void saveItem_ShouldSaveItem() {

        WorkoutPlanItem item = new WorkoutPlanItem();

        when(workoutPlanItemRepository.save(item))
                .thenReturn(item);

        WorkoutPlanItem result =
                workoutPlanItemService.saveItem(item);

        assertNotNull(result);
        verify(workoutPlanItemRepository).save(item);
    }

    @Test
    void deleteItem_ShouldDeleteItem() {

        workoutPlanItemService.deleteItem(10);

        verify(workoutPlanItemRepository).deleteById(10);
    }
}