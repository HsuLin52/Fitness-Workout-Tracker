package com.example.FitnessWorkoutTracker.exception;

// Thrown when a completed workout, or a resource needed to record/display one
// (a workout plan, plan item, or exercise), cannot be found by ID.
public class WorkoutNotFoundException extends RuntimeException {

    public WorkoutNotFoundException(String message) {
        super(message);
    }
}
