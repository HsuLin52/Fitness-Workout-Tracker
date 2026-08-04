package com.example.FitnessWorkoutTracker.dto;

import com.example.FitnessWorkoutTracker.model.ExerciseType;

import java.time.LocalDate;

// Stores the calculated dashboard totals for one date period.
public class DashboardStats {

    private final String periodLabel;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private final long completedWorkoutCount;
    private final double totalCalories;
    private final long totalMinutes;
    private final double averageDuration;

    private final ExerciseType mostFrequentExerciseType;

    public DashboardStats(
            String periodLabel,
            LocalDate startDate,
            LocalDate endDate,
            long completedWorkoutCount,
            double totalCalories,
            long totalMinutes,
            double averageDuration,
            ExerciseType mostFrequentExerciseType
    ) {
        this.periodLabel = periodLabel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completedWorkoutCount = completedWorkoutCount;
        this.totalCalories = totalCalories;
        this.totalMinutes = totalMinutes;
        this.averageDuration = averageDuration;
        this.mostFrequentExerciseType = mostFrequentExerciseType;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getCompletedWorkoutCount() {
        return completedWorkoutCount;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public ExerciseType getMostFrequentExerciseType() {
        return mostFrequentExerciseType;
    }

    // Allows the dashboard page to easily check whether
    // the selected period contains any completed workouts.
    public boolean hasWorkouts() {
        return completedWorkoutCount > 0;
    }

    // Converts enum values such as STRENGTH into Strength.
    // A friendly message is returned when there is no workout data.
    public String getMostFrequentExerciseTypeLabel() {
        if (mostFrequentExerciseType == null) {
            return "No workout data";
        }

        String typeName =
                mostFrequentExerciseType.name()
                        .toLowerCase()
                        .replace('_', ' ');

        return Character.toUpperCase(typeName.charAt(0))
                + typeName.substring(1);
    }
}