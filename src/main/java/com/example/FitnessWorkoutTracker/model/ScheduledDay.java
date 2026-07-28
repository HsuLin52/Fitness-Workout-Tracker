package com.example.FitnessWorkoutTracker.model;

public enum ScheduledDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    @Override
    public String toString() {
        String value = name().toLowerCase();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
