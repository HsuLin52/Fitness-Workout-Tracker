package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import com.example.FitnessWorkoutTracker.repository.UserRepository;
import com.example.FitnessWorkoutTracker.service.CompletedWorkoutService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Handles web requests for recording, viewing, editing, and deleting completed workouts
@Controller
@RequestMapping("/workouts")
public class CompletedWorkoutController {

    private final CompletedWorkoutService completedWorkoutService;
    private final WorkoutPlanService workoutPlanService;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public CompletedWorkoutController(
            CompletedWorkoutService completedWorkoutService,
            WorkoutPlanService workoutPlanService,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.completedWorkoutService = completedWorkoutService;
        this.workoutPlanService = workoutPlanService;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // Displays every completed workout. The full searchable/paginated history page lives at /history (Member 4)
    @GetMapping
    public String listWorkouts(Model model) {
        model.addAttribute("workouts", completedWorkoutService.getAllWorkouts());
        return "workout-list";
    }

    // Shows the form for recording a new completed workout, optionally pre-filled from a saved plan
    @GetMapping("/add")
    public String showAddForm(
            @RequestParam(required = false) Integer planId,
            @RequestParam(required = false) Integer exerciseId,
            Model model
    ) {

        CompletedWorkout completedWorkout = new CompletedWorkout();

        if (planId != null) {
            WorkoutPlan plan = workoutPlanService
                    .getPlanById(planId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Workout plan not found with ID: " + planId));
            completedWorkout.setWorkoutPlan(plan);
        }

        if (exerciseId != null) {
            Exercise exercise = exerciseRepository
                    .findById(exerciseId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Exercise not found with ID: " + exerciseId));
            completedWorkout.setExercise(exercise);
        }

        model.addAttribute("completedWorkout", completedWorkout);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("exercises", exerciseRepository.findAll());
        model.addAttribute("plans", workoutPlanService.getAllPlans());
        model.addAttribute("isEdit", false);

        return "workout-form";
    }

    // Saves a newly recorded completed workout, calculating calories from the exercise's calorie rate
    @PostMapping("/add")
    public String addWorkout(
            @Valid @ModelAttribute("completedWorkout") CompletedWorkout completedWorkout,
            BindingResult result,
            @RequestParam("userId") Integer userId,
            @RequestParam("exerciseId") Integer exerciseId,
            @RequestParam(value = "planId", required = false) Integer planId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", false);
            return "workout-form";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        completedWorkout.setUser(user);
        completedWorkout.setExercise(exercise);

        if (planId != null) {
            WorkoutPlan plan = workoutPlanService.getPlanById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Workout plan not found"));
            completedWorkout.setWorkoutPlan(plan);
        } else {
            completedWorkout.setWorkoutPlan(null);
        }

        CompletedWorkout saved = completedWorkoutService.saveWorkout(completedWorkout);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Workout recorded successfully. Calories burned: " + saved.getCalories()
        );

        return "redirect:/workouts/" + saved.getId();
    }

    // Displays the details of one completed workout
    @GetMapping("/{id}")
    public String viewWorkout(
            @PathVariable Integer id,
            Model model
    ) {

        CompletedWorkout completedWorkout = completedWorkoutService
                .getWorkoutById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Completed workout not found with ID: " + id));

        model.addAttribute("completedWorkout", completedWorkout);

        return "workout-details";
    }

    // Shows the form for editing an existing completed workout
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Integer id,
            Model model
    ) {

        CompletedWorkout completedWorkout = completedWorkoutService
                .getWorkoutById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Completed workout not found with ID: " + id));

        model.addAttribute("completedWorkout", completedWorkout);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("exercises", exerciseRepository.findAll());
        model.addAttribute("plans", workoutPlanService.getAllPlans());
        model.addAttribute("isEdit", true);

        return "workout-form";
    }

    // Updates an existing completed workout, recalculating calories from the (possibly changed) exercise and duration
    @PostMapping("/edit")
    public String updateWorkout(
            @Valid @ModelAttribute("completedWorkout") CompletedWorkout completedWorkout,
            BindingResult result,
            @RequestParam("userId") Integer userId,
            @RequestParam("exerciseId") Integer exerciseId,
            @RequestParam(value = "planId", required = false) Integer planId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", true);
            return "workout-form";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        completedWorkout.setUser(user);
        completedWorkout.setExercise(exercise);

        if (planId != null) {
            WorkoutPlan plan = workoutPlanService.getPlanById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Workout plan not found"));
            completedWorkout.setWorkoutPlan(plan);
        } else {
            completedWorkout.setWorkoutPlan(null);
        }

        CompletedWorkout saved = completedWorkoutService.saveWorkout(completedWorkout);

        redirectAttributes.addFlashAttribute("successMessage", "Workout updated successfully.");

        return "redirect:/workouts/" + saved.getId();
    }

    // Deletes a completed workout by ID
    @GetMapping("/delete/{id}")
    public String deleteWorkout(@PathVariable Integer id) {

        if (completedWorkoutService.getWorkoutById(id).isEmpty()) {
            throw new IllegalArgumentException("Completed workout not found with ID: " + id);
        }

        completedWorkoutService.deleteWorkout(id);

        return "redirect:/workouts";
    }
}
