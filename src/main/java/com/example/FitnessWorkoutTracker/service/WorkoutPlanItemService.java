package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import com.example.FitnessWorkoutTracker.repository.WorkoutPlanItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkoutPlanItemService {

    private final WorkoutPlanItemRepository workoutPlanItemRepository;

    public WorkoutPlanItemService(
            WorkoutPlanItemRepository workoutPlanItemRepository
    ) {
        this.workoutPlanItemRepository = workoutPlanItemRepository;
    }

    // Return all items for one workout plan
    public List<WorkoutPlanItem> getItemsByPlan(WorkoutPlan workoutPlan) {
        return workoutPlanItemRepository.findByWorkoutPlan(workoutPlan);
    }

    // Return one item by ID
    public Optional<WorkoutPlanItem> getItemById(Integer id) {
        return workoutPlanItemRepository.findById(id);
    }

    // Save or update an item
    public WorkoutPlanItem saveItem(WorkoutPlanItem workoutPlanItem) {
        return workoutPlanItemRepository.save(workoutPlanItem);
    }

    // Delete an item
    public void deleteItem(Integer id) {
        workoutPlanItemRepository.deleteById(id);
    }
}
