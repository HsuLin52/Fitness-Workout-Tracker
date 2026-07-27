package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPlanItemRepository extends JpaRepository<WorkoutPlanItem, Integer> {

    List<WorkoutPlanItem> findByWorkoutPlan(WorkoutPlan workoutPlan);

}