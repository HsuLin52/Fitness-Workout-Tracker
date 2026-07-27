package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.UserRepository;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanItemService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;
    private final WorkoutPlanItemService workoutPlanItemService;
    private final UserRepository userRepository;

    public WorkoutPlanController(
            WorkoutPlanService workoutPlanService,
            WorkoutPlanItemService workoutPlanItemService,
            UserRepository userRepository
    ) {
        this.workoutPlanService = workoutPlanService;
        this.workoutPlanItemService = workoutPlanItemService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listPlans(Model model) {
        model.addAttribute("plans", workoutPlanService.getAllPlans());
        return "plan-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("workoutPlan", new WorkoutPlan());
        model.addAttribute("users", userRepository.findAll());
        return "plan-form";
    }

    @PostMapping("/add")
    public String addPlan(
            @Valid @ModelAttribute("workoutPlan") WorkoutPlan workoutPlan,
            BindingResult result,
            @RequestParam("userId") Integer userId,
            Model model
    ) {

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            return "plan-form";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        workoutPlan.setUser(user);

        workoutPlanService.savePlan(workoutPlan);

        return "redirect:/plans";
    }

    @GetMapping("/{id}")
    public String viewPlan(
            @PathVariable Integer id,
            Model model
    ) {

        WorkoutPlan workoutPlan = workoutPlanService
                .getPlanById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workout plan not found with ID: " + id
                        )
                );

        model.addAttribute("workoutPlan", workoutPlan);
        model.addAttribute(
                "items",
                workoutPlanItemService.getItemsByPlan(workoutPlan)
        );

        return "plan-details";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Integer id,
            Model model
    ) {

        WorkoutPlan workoutPlan = workoutPlanService
                .getPlanById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workout plan not found with ID: " + id
                        )
                );

        model.addAttribute("workoutPlan", workoutPlan);
        model.addAttribute("users", userRepository.findAll());

        return "plan-form";
    }

    @PostMapping("/edit")
    public String updatePlan(
            @Valid @ModelAttribute("workoutPlan") WorkoutPlan workoutPlan,
            BindingResult result,
            @RequestParam("userId") Integer userId,
            Model model
    ) {

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            return "plan-form";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        workoutPlan.setUser(user);

        workoutPlanService.savePlan(workoutPlan);

        return "redirect:/plans";
    }

    @GetMapping("/delete/{id}")
    public String deletePlan(@PathVariable Integer id) {

        if (workoutPlanService.getPlanById(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Workout plan not found with ID: " + id
            );
        }

        workoutPlanService.deletePlan(id);

        return "redirect:/plans";
    }
}