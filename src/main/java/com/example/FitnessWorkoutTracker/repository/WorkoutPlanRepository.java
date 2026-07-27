package com.example.FitnessWorkoutTracker.repository;

import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Integer> {

    List<WorkoutPlan> findByUser(User user);

}
