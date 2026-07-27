package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;

    public WorkoutPlanService(WorkoutPlanRepository workoutPlanRepository) {
        this.workoutPlanRepository = workoutPlanRepository;
    }

    // Return all workout plans
    public List<WorkoutPlan> getAllPlans() {
        return workoutPlanRepository.findAll();
    }

    // Return one workout plan by ID
    public Optional<WorkoutPlan> getPlanById(Integer id) {
        return workoutPlanRepository.findById(id);
    }

    // Save or update a workout plan
    public WorkoutPlan savePlan(WorkoutPlan workoutPlan) {
        return workoutPlanRepository.save(workoutPlan);
    }

    // Delete workout plan
    public void deletePlan(Integer id) {
        workoutPlanRepository.deleteById(id);
    }

    // Get all plans for one user
    public List<WorkoutPlan> getPlansByUser(User user) {
        return workoutPlanRepository.findByUser(user);
    }
}