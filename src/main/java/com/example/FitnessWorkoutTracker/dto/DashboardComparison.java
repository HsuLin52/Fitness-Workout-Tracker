package com.example.FitnessWorkoutTracker.dto;

// Stores the comparison between the selected current period
// and the equivalent previous period.
//
// Examples:
// - This week compared with last week
// - This month compared with last month
public class DashboardComparison {

    private final DashboardStats currentPeriod;
    private final DashboardStats previousPeriod;

    private final long workoutCountDifference;
    private final double calorieDifference;
    private final long minuteDifference;
    private final double averageDurationDifference;

    private final double workoutCountPercentageChange;
    private final double caloriePercentageChange;
    private final double minutePercentageChange;

    private final int currentWorkoutBarPercentage;
    private final int previousWorkoutBarPercentage;

    private final int currentCalorieBarPercentage;
    private final int previousCalorieBarPercentage;

    private final int currentMinuteBarPercentage;
    private final int previousMinuteBarPercentage;

    public DashboardComparison(
            DashboardStats currentPeriod,
            DashboardStats previousPeriod
    ) {
        this.currentPeriod = currentPeriod;
        this.previousPeriod = previousPeriod;

        this.workoutCountDifference =
                currentPeriod.getCompletedWorkoutCount()
                        - previousPeriod.getCompletedWorkoutCount();

        this.calorieDifference =
                currentPeriod.getTotalCalories()
                        - previousPeriod.getTotalCalories();

        this.minuteDifference =
                currentPeriod.getTotalMinutes()
                        - previousPeriod.getTotalMinutes();

        this.averageDurationDifference =
                currentPeriod.getAverageDuration()
                        - previousPeriod.getAverageDuration();

        this.workoutCountPercentageChange =
                calculatePercentageChange(
                        currentPeriod.getCompletedWorkoutCount(),
                        previousPeriod.getCompletedWorkoutCount()
                );

        this.caloriePercentageChange =
                calculatePercentageChange(
                        currentPeriod.getTotalCalories(),
                        previousPeriod.getTotalCalories()
                );

        this.minutePercentageChange =
                calculatePercentageChange(
                        currentPeriod.getTotalMinutes(),
                        previousPeriod.getTotalMinutes()
                );

        long maximumWorkoutCount = Math.max(
                currentPeriod.getCompletedWorkoutCount(),
                previousPeriod.getCompletedWorkoutCount()
        );

        double maximumCalories = Math.max(
                currentPeriod.getTotalCalories(),
                previousPeriod.getTotalCalories()
        );

        long maximumMinutes = Math.max(
                currentPeriod.getTotalMinutes(),
                previousPeriod.getTotalMinutes()
        );

        this.currentWorkoutBarPercentage =
                calculateBarPercentage(
                        currentPeriod.getCompletedWorkoutCount(),
                        maximumWorkoutCount
                );

        this.previousWorkoutBarPercentage =
                calculateBarPercentage(
                        previousPeriod.getCompletedWorkoutCount(),
                        maximumWorkoutCount
                );

        this.currentCalorieBarPercentage =
                calculateBarPercentage(
                        currentPeriod.getTotalCalories(),
                        maximumCalories
                );

        this.previousCalorieBarPercentage =
                calculateBarPercentage(
                        previousPeriod.getTotalCalories(),
                        maximumCalories
                );

        this.currentMinuteBarPercentage =
                calculateBarPercentage(
                        currentPeriod.getTotalMinutes(),
                        maximumMinutes
                );

        this.previousMinuteBarPercentage =
                calculateBarPercentage(
                        previousPeriod.getTotalMinutes(),
                        maximumMinutes
                );
    }

    public DashboardStats getCurrentPeriod() {
        return currentPeriod;
    }

    public DashboardStats getPreviousPeriod() {
        return previousPeriod;
    }

    public long getWorkoutCountDifference() {
        return workoutCountDifference;
    }

    public double getCalorieDifference() {
        return calorieDifference;
    }

    public long getMinuteDifference() {
        return minuteDifference;
    }

    public double getAverageDurationDifference() {
        return averageDurationDifference;
    }

    public double getWorkoutCountPercentageChange() {
        return workoutCountPercentageChange;
    }

    public double getCaloriePercentageChange() {
        return caloriePercentageChange;
    }

    public double getMinutePercentageChange() {
        return minutePercentageChange;
    }

    public int getCurrentWorkoutBarPercentage() {
        return currentWorkoutBarPercentage;
    }

    public int getPreviousWorkoutBarPercentage() {
        return previousWorkoutBarPercentage;
    }

    public int getCurrentCalorieBarPercentage() {
        return currentCalorieBarPercentage;
    }

    public int getPreviousCalorieBarPercentage() {
        return previousCalorieBarPercentage;
    }

    public int getCurrentMinuteBarPercentage() {
        return currentMinuteBarPercentage;
    }

    public int getPreviousMinuteBarPercentage() {
        return previousMinuteBarPercentage;
    }

    // Returns a simple label used by the dashboard page.
    public String getWorkoutCountTrend() {
        return trendLabel(workoutCountDifference);
    }

    public String getCalorieTrend() {
        return trendLabel(calorieDifference);
    }

    public String getMinuteTrend() {
        return trendLabel(minuteDifference);
    }

    // Calculates percentage change using the previous period as the base.
    //
    // When both periods are zero, the change is 0%.
    // When the previous value is zero but the current value is greater,
    // the increase is represented as 100%.
    private double calculatePercentageChange(
            double currentValue,
            double previousValue
    ) {
        if (previousValue == 0) {
            return currentValue == 0 ? 0.0 : 100.0;
        }

        return (
                (currentValue - previousValue)
                        / previousValue
        ) * 100.0;
    }

    // Converts two comparison values into safe bar widths from 0 to 100.
    private int calculateBarPercentage(
            double value,
            double maximumValue
    ) {
        if (maximumValue <= 0 || value <= 0) {
            return 0;
        }

        return (int) Math.round(
                (value / maximumValue) * 100.0
        );
    }

    private String trendLabel(double difference) {
        if (difference > 0) {
            return "increase";
        }

        if (difference < 0) {
            return "decrease";
        }

        return "no-change";
    }
}