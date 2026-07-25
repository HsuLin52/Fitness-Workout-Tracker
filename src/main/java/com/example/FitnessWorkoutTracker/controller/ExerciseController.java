package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handles web requests related to exercises
@Controller
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    // Constructor
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    // Method that displays all exercises
    @GetMapping
    public String listExercises(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ExerciseType type,
            Model model
    ) {

        List<Exercise> exercises; // stores the exercises that should appear on the page

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty(); // Checks whether the user entered a non-empty search keyword

        // If both a keyword and exercise type are provided, the exercise is searched using both conditions
        if (hasKeyword && type != null) {
            exercises = exerciseService.searchByNameAndType(
                    keyword.trim(),
                    type
            );
        // If only a keyword is provided, the exercise is searched by name
        } else if (hasKeyword) {
            exercises = exerciseService.searchByName(keyword.trim());
        // If only an exercise type is selected, the exercise is filtered by that type
        } else if (type != null) {
            exercises = exerciseService.searchByType(type);
        // If no search conditions are provided, every exercise in the database is displayed
        } else {
            exercises = exerciseService.getAllExercises();
        }

        model.addAttribute("exercises", exercises); // Adds the resulting exercise list to the model
        model.addAttribute("exerciseTypes", ExerciseType.values()); // Sends all enum values to the page, so they can be displayed in the type dropdown
        model.addAttribute("keyword", keyword); // Sends the current keyword back to the page so the search box keeps the entered value
        model.addAttribute("selectedType", type); // Sends the selected type back to the page return "exercise-list"

        return "exercise-list";
    }

    // Method that displays the form for creating a new exercise.
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("exercise", new Exercise()); // Adds an empty Exercise object for form binding
        model.addAttribute("exerciseTypes", ExerciseType.values()); // Adds all exercise categories for the dropdown menu

        return "exercise-form";
    }

    // Method that saves a new exercise
    @PostMapping("/add")
    public String addExercise(
            @Valid @ModelAttribute("exercise") Exercise exercise,
            BindingResult result,
            Model model
    ) {

        // Checks whether the submitted exercise contains any validation errors
        if (result.hasErrors()) {
            model.addAttribute("exerciseTypes", ExerciseType.values());
            return "exercise-form";
        }

        exerciseService.saveExercise(exercise); // Saves the valid exercise in the database

        return "redirect:/exercises";
    }

    // Method that displays details for one exercise
    @GetMapping("/{id}")
    public String viewExercise(
            @PathVariable Integer id,
            Model model
    ) {

        // Searches for the exercise using its ID
        Exercise exercise = exerciseService
                .getExerciseById(id)
                // Returns the exercise when it exists
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exercise not found with ID: " + id
                        )
                );

        model.addAttribute("exercise", exercise); // Adds the exercise to the model

        return "exercise-details";
    }

    // Method that displays the form for editing an existing exercise
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Integer id,
            Model model
    ) {

        // Searches for the existing exercise using its ID
        Exercise exercise = exerciseService
                .getExerciseById(id)
                // If the exercise does not exist, an exception is thrown
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exercise not found with ID: " + id
                        )
                );

        model.addAttribute("exercise", exercise); // Adds the existing exercise to the model
        model.addAttribute("exerciseTypes", ExerciseType.values()); // Adds all available exercise types so the form can display them in the dropdown menu

        return "exercise-form";
    }

    // Method that updates an existing exercise
    @PostMapping("/edit")
    public String updateExercise(
            @Valid @ModelAttribute("exercise") Exercise exercise,
            BindingResult result,
            Model model
    ) {

        // Checks whether the submitted form contains validation errors
        if (result.hasErrors()) {
            model.addAttribute("exerciseTypes", ExerciseType.values()); // Adds all exercise types again so the dropdown menu can be displayed when the form is reloaded
            return "exercise-form";
        }

        exerciseService.saveExercise(exercise); // Saves the Exercise object

        return "redirect:/exercises";
    }

    // Method that deletes an exercise by ID
    @GetMapping("/delete/{id}")
    public String deleteExercise(@PathVariable Integer id) {

        // Checks whether an exercise with the requested ID exists
        if (exerciseService.getExerciseById(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Exercise not found with ID: " + id
            );
        }

        exerciseService.deleteExercise(id); // Deletes the exercise from the database

        return "redirect:/exercises";
    }
}