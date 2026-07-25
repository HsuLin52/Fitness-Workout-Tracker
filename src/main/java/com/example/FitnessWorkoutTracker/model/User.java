package com.example.FitnessWorkoutTracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Represents a user of the Fitness Workout Tracker application.
@Entity
@Table(name = "app_user")
public class User {

    // Primary key for the user table which is generated automatically by MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Stores the user's full name
    @NotBlank(message = "Name is required") // The name cannot be blank
    @Size(max = 100, message = "Name cannot exceed 100 characters") // The name cannot exceed 100 characters
    private String name;

    // Stores the user's email address
    @NotBlank(message = "Email is required") // The email cannot be blank
    @Email(message = "Enter a valid email address") // The email must follow a valid email format
    @Column(unique = true) // The email must be unique in the database
    private String email;

    // Default constructor
    public User() {
    }

    // Constructor used when creating a new user
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getter method for ID
    public Integer getId() {
        return id;
    }

    // Setter method for ID
    public void setId(Integer id) {
        this.id = id;
    }

    // Getter method for User's name
    public String getName() {
        return name;
    }

    // Setter method for User's name
    public void setName(String name) {
        this.name = name;
    }

    // Getter method for Email
    public String getEmail() {
        return email;
    }

    // Setter method for Email
    public void setEmail(String email) {
        this.email = email;
    }
}