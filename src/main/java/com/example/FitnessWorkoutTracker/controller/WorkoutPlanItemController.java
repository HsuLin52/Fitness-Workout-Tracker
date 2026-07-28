package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanItemService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/plans/{planId}/items")
public class WorkoutPlanItemController {

    private final WorkoutPlanService workoutPlanService;
    private final WorkoutPlanItemService workoutPlanItemService;
    private final ExerciseRepository exerciseRepository;

    public WorkoutPlanItemController(
            WorkoutPlanService workoutPlanService,
            WorkoutPlanItemService workoutPlanItemService,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutPlanService = workoutPlanService;
        this.workoutPlanItemService = workoutPlanItemService;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping("/add")
    public String showAddForm(
            @PathVariable Integer planId,
            Model model
    ) {

        WorkoutPlan plan = workoutPlanService
                .getPlanById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan not found"));

        WorkoutPlanItem item = new WorkoutPlanItem();
        item.setWorkoutPlan(plan);

        model.addAttribute("plan", plan);
        model.addAttribute("workoutPlanItem", item);
        model.addAttribute("exercises", exerciseRepository.findAll());
        model.addAttribute("isEdit", false);

        return "plan-item-form";
    }

    @PostMapping("/add")
    public String addItem(
            @PathVariable Integer planId,
            @Valid @ModelAttribute WorkoutPlanItem workoutPlanItem,
            BindingResult result,
            @RequestParam Integer exerciseId,
            Model model
    ) {

        WorkoutPlan plan = workoutPlanService
                .getPlanById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan not found"));

        if (result.hasErrors()) {

            model.addAttribute("plan", plan);
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("isEdit", false);

            return "plan-item-form";
        }

        Exercise exercise = exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exercise not found"));

        workoutPlanItem.setWorkoutPlan(plan);
        workoutPlanItem.setExercise(exercise);

        workoutPlanItemService.saveItem(workoutPlanItem);

        return "redirect:/plans/" + planId;
    }

    @GetMapping("/edit/{itemId}")
    public String showEditForm(
            @PathVariable Integer planId,
            @PathVariable Integer itemId,
            Model model
    ) {

        WorkoutPlan plan = workoutPlanService
                .getPlanById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan not found"));

        WorkoutPlanItem item = workoutPlanItemService
                .getItemById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan item not found"));

        if (!item.getWorkoutPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Workout plan item does not belong to this workout plan");
        }

        model.addAttribute("plan", plan);
        model.addAttribute("workoutPlanItem", item);
        model.addAttribute("exercises", exerciseRepository.findAll());
        model.addAttribute("isEdit", true);

        return "plan-item-form";
    }

    @PostMapping("/edit")
    public String updateItem(
            @PathVariable Integer planId,
            @Valid @ModelAttribute WorkoutPlanItem workoutPlanItem,
            BindingResult result,
            @RequestParam Integer exerciseId,
            Model model
    ) {

        WorkoutPlan plan = workoutPlanService
                .getPlanById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan not found"));

        WorkoutPlanItem existingItem = workoutPlanItemService
                .getItemById(workoutPlanItem.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan item not found"));

        if (!existingItem.getWorkoutPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Workout plan item does not belong to this workout plan");
        }

        if (result.hasErrors()) {

            model.addAttribute("plan", plan);
            model.addAttribute("workoutPlanItem", workoutPlanItem);
            model.addAttribute("exercises", exerciseRepository.findAll());
            model.addAttribute("isEdit", true);

            return "plan-item-form";
        }

        Exercise exercise = exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exercise not found"));

        existingItem.setExercise(exercise);
        existingItem.setSets(workoutPlanItem.getSets());
        existingItem.setReps(workoutPlanItem.getReps());
        existingItem.setTargetMinutes(workoutPlanItem.getTargetMinutes());

        workoutPlanItemService.saveItem(existingItem);

        return "redirect:/plans/" + planId;
    }

    @GetMapping("/delete/{itemId}")
    public String deleteItem(
            @PathVariable Integer planId,
            @PathVariable Integer itemId
    ) {

        WorkoutPlanItem item = workoutPlanItemService
                .getItemById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout plan item not found"));

        if (!item.getWorkoutPlan().getId().equals(planId)) {
            throw new IllegalArgumentException("Workout plan item does not belong to this workout plan");
        }

        workoutPlanItemService.deleteItem(itemId);

        return "redirect:/plans/" + planId;
    }

}