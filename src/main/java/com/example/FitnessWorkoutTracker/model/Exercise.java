package com.example.FitnessWorkoutTracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Represents an exercise in the Fitness Workout Tracker application
@Entity
@Table(name = "exercise")
public class Exercise {

    // Primary key for the exercise table which is generated automatically by MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Stores the exercise name
    @NotBlank(message = "Exercise name is required") // The name cannot be blank
    @Size(max = 100, message = "Exercise name cannot exceed 100 characters") // The name cannot exceed 100 characters
    private String name;

    // Stores the category of the exercise
    @NotNull(message = "Exercise type is required") // The exercise type cannot be null
    @Enumerated(EnumType.STRING) // stores values such as CARDIO, STRENGTH or more
    private ExerciseType type;

    // Stores the estimated calories burned per minute
    @NotNull(message = "Calories per minute is required") // Calories per minute cannot be null
    @DecimalMin(
            value = "0.1",
            message = "Calories per minute must be greater than 0"
    ) // Calories per minute must be greater than 0
    private Double caloriesPerMinute;

    // Default constructor
    public Exercise() {
    }

    // Constructor used when creating a new exercise
    public Exercise(
            String name,
            ExerciseType type,
            Double caloriesPerMinute
    ) {
        this.name = name;
        this.type = type;
        this.caloriesPerMinute = caloriesPerMinute;
    }

    // Getter method for ID
    public Integer getId() {
        return id;
    }

    // Setter method for ID
    public void setId(Integer id) {
        this.id = id;
    }

    // Getter method for Exercise name
    public String getName() {
        return name;
    }

    // Setter method for Exercise name
    public void setName(String name) {
        this.name = name;
    }

    // Getter method for Exercise category
    public ExerciseType getType() {
        return type;
    }

    // Setter method for Exercise category
    public void setType(ExerciseType type) {
        this.type = type;
    }

    // Getter method for the estimated calories burned per minute
    public Double getCaloriesPerMinute() {
        return caloriesPerMinute;
    }

    // Setter method for the estimated calories burned per minute
    public void setCaloriesPerMinute(Double caloriesPerMinute) {
        this.caloriesPerMinute = caloriesPerMinute;
    }
}