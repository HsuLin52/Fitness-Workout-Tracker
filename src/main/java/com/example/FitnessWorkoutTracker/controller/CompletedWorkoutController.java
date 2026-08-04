package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import com.example.FitnessWorkoutTracker.exception.WorkoutNotFoundException;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import com.example.FitnessWorkoutTracker.repository.UserRepository;
import com.example.FitnessWorkoutTracker.service.CompletedWorkoutService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanItemService;
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
    private final WorkoutPlanItemService workoutPlanItemService;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public CompletedWorkoutController(
            CompletedWorkoutService completedWorkoutService,
            WorkoutPlanService workoutPlanService,
            WorkoutPlanItemService workoutPlanItemService,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.completedWorkoutService = completedWorkoutService;
        this.workoutPlanService = workoutPlanService;
        this.workoutPlanItemService = workoutPlanItemService;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // Displays every completed workout. The full searchable/paginated history page lives at /history (Member 4)
    @GetMapping
    public String listWorkouts(Model model) {
        model.addAttribute("workouts", completedWorkoutService.getAllWorkouts());
        return "workout-list";
    }

    // Shows the form for recording a new completed workout. When itemId is supplied (from the
    // "Record Workout" button next to an exercise in a plan) the user, exercise, sets, reps, and
    // target duration are pre-filled from that plan item.
    @GetMapping("/add")
    public String showAddForm(
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) Integer planId,
            @RequestParam(required = false) Integer exerciseId,
            Model model
    ) {

        CompletedWorkout completedWorkout = new CompletedWorkout();

        if (itemId != null) {
            WorkoutPlanItem item = workoutPlanItemService
                    .getItemById(itemId)
                    .orElseThrow(() ->
                            new WorkoutNotFoundException("Workout plan item not found with ID: " + itemId));

            completedWorkout.setWorkoutPlan(item.getWorkoutPlan());
            completedWorkout.setExercise(item.getExercise());
            completedWorkout.setUser(item.getWorkoutPlan().getUser());
            completedWorkout.setSets(item.getSets());
            completedWorkout.setReps(item.getReps());
            completedWorkout.setDuration(item.getTargetMinutes());
        } else {

            if (planId != null) {
                WorkoutPlan plan = workoutPlanService
                        .getPlanById(planId)
                        .orElseThrow(() ->
                                new WorkoutNotFoundException("Workout plan not found with ID: " + planId));
                completedWorkout.setWorkoutPlan(plan);
                completedWorkout.setUser(plan.getUser());
            }

            if (exerciseId != null) {
                Exercise exercise = exerciseRepository
                        .findById(exerciseId)
                        .orElseThrow(() ->
                                new WorkoutNotFoundException("Exercise not found with ID: " + exerciseId));
                completedWorkout.setExercise(exercise);
            }
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
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "exerciseId", required = false) Integer exerciseId,
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

        try {
            applyRelations(completedWorkout, userId, exerciseId, planId);
        } catch (WorkoutValidationException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", false);
            return "workout-form";
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
                        new WorkoutNotFoundException("Completed workout not found with ID: " + id));

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
                        new WorkoutNotFoundException("Completed workout not found with ID: " + id));

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
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "exerciseId", required = false) Integer exerciseId,
            @RequestParam(value = "planId", required = false) Integer planId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // Confirm the record being edited still exists before any update is attempted,
        // rather than silently inserting a new row for an ID that no longer exists.
        if (completedWorkout.getId() == null
                || completedWorkoutService.getWorkoutById(completedWorkout.getId()).isEmpty()) {
            model.addAttribute("formError", "The workout you are trying to update could not be found.");
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", true);
            return "workout-form";
        }

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", true);
            return "workout-form";
        }

        try {
            applyRelations(completedWorkout, userId, exerciseId, planId);
        } catch (WorkoutValidationException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("plans", workoutPlanService.getAllPlans());
            model.addAttribute("isEdit", true);
            return "workout-form";
        }

        CompletedWorkout saved = completedWorkoutService.saveWorkout(completedWorkout);

        redirectAttributes.addFlashAttribute("successMessage", "Workout updated successfully.");

        return "redirect:/workouts/" + saved.getId();
    }

    // Deletes a completed workout by ID. Uses POST since this mutates data and should not be
    // triggerable by a plain link/prefetch the way a GET request is.
    @PostMapping("/delete/{id}")
    public String deleteWorkout(@PathVariable Integer id) {

        if (completedWorkoutService.getWorkoutById(id).isEmpty()) {
            throw new WorkoutNotFoundException("Completed workout not found with ID: " + id);
        }

        completedWorkoutService.deleteWorkout(id);

        return "redirect:/workouts";
    }

    // Resolves the user, exercise, and optional plan for a submitted form, enforcing that
    // an exercise belongs to the selected plan and that the plan belongs to the selected user.
    // Throws WorkoutValidationException with a friendly message on any failure.
    private void applyRelations(
            CompletedWorkout completedWorkout,
            Integer userId,
            Integer exerciseId,
            Integer planId
    ) {

        if (userId == null) {
            throw new WorkoutValidationException("Please select a user.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WorkoutValidationException("Selected user could not be found."));

        if (exerciseId == null) {
            throw new WorkoutValidationException("Please select an exercise.");
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new WorkoutValidationException("Selected exercise could not be found."));

        WorkoutPlan plan = null;

        if (planId != null) {

            plan = workoutPlanService.getPlanById(planId)
                    .orElseThrow(() -> new WorkoutValidationException("Selected workout plan could not be found."));

            if (plan.getUser() == null || !plan.getUser().getId().equals(user.getId())) {
                throw new WorkoutValidationException("Selected workout plan does not belong to the selected user.");
            }

            boolean exerciseInPlan = workoutPlanItemService.getItemsByPlan(plan).stream()
                    .anyMatch(item ->
                            item.getExercise() != null
                                    && item.getExercise().getId().equals(exercise.getId())
                    );

            if (!exerciseInPlan) {
                throw new WorkoutValidationException("Selected exercise is not part of the selected workout plan.");
            }
        }

        completedWorkout.setUser(user);
        completedWorkout.setExercise(exercise);
        completedWorkout.setWorkoutPlan(plan);
    }

    // Thrown when submitted form selections fail cross-field validation
    private static class WorkoutValidationException extends RuntimeException {
        WorkoutValidationException(String message) {
            super(message);
        }
    }
}
