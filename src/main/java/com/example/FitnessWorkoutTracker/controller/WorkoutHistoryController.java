package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.exception.WorkoutNotFoundException;
import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.service.CompletedWorkoutService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Member 4: searchable, filterable, sortable,
// and paginated completed-workout history.
@Controller
@RequestMapping("/history")
public class WorkoutHistoryController {

    private final CompletedWorkoutService completedWorkoutService;
    private final WorkoutPlanService workoutPlanService;

    public WorkoutHistoryController(
            CompletedWorkoutService completedWorkoutService,
            WorkoutPlanService workoutPlanService
    ) {
        this.completedWorkoutService = completedWorkoutService;
        this.workoutPlanService = workoutPlanService;
    }

    @GetMapping
    public String showHistory(
            @RequestParam(defaultValue = "") String search,

            @RequestParam(required = false)
            String type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            Double minCalories,

            @RequestParam(required = false)
            Integer planId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "workoutDate")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model
    ) {
        ExerciseType selectedType = parseExerciseType(type, model);

        String safeSortField =
                completedWorkoutService.sanitizeSortField(sortField);

        String safeSortDir =
                completedWorkoutService.sanitizeSortDirection(sortDir);

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            model.addAttribute(
                    "filterError",
                    "Start date cannot be after end date."
            );
        }

        if (minCalories != null && minCalories < 0) {
            model.addAttribute(
                    "filterError",
                    "Minimum calories cannot be negative."
            );

            minCalories = 0.0;
        }

        Page<CompletedWorkout> historyPage =
                completedWorkoutService.searchHistory(
                        search,
                        selectedType,
                        startDate,
                        endDate,
                        minCalories,
                        planId,
                        page,
                        size,
                        safeSortField,
                        safeSortDir
                );

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("workouts", historyPage.getContent());

        model.addAttribute(
                "pageNumbers",
                buildPageNumbers(historyPage)
        );

        model.addAttribute(
                "exerciseTypes",
                ExerciseType.values()
        );

        model.addAttribute(
                "plans",
                workoutPlanService.getAllPlans()
        );

        addActiveQueryParameters(
                model,
                search,
                selectedType,
                startDate,
                endDate,
                minCalories,
                planId,
                historyPage.getSize(),
                safeSortField,
                safeSortDir
        );

        return "history-list";
    }

    @GetMapping("/{id}")
    public String showHistoryDetails(
            @PathVariable Integer id,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            String type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            Double minCalories,

            @RequestParam(required = false)
            Integer planId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "workoutDate")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model
    ) {
        CompletedWorkout workout =
                completedWorkoutService.getWorkoutById(id)
                        .filter(item ->
                                Boolean.TRUE.equals(item.getCompleted())
                        )
                        .orElseThrow(() ->
                                new WorkoutNotFoundException(
                                        "Completed workout not found with ID: "
                                                + id
                                )
                        );

        ExerciseType selectedType = parseExerciseType(type, model);

        String safeSortField =
                completedWorkoutService.sanitizeSortField(sortField);

        String safeSortDir =
                completedWorkoutService.sanitizeSortDirection(sortDir);

        model.addAttribute("completedWorkout", workout);
        model.addAttribute("page", Math.max(page, 0));

        addActiveQueryParameters(
                model,
                search,
                selectedType,
                startDate,
                endDate,
                minCalories,
                planId,
                size,
                safeSortField,
                safeSortDir
        );

        return "history-details";
    }

    private ExerciseType parseExerciseType(
            String type,
            Model model
    ) {
        if (type == null || type.isBlank()) {
            return null;
        }

        try {
            return ExerciseType.valueOf(
                    type.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute(
                    "filterError",
                    "The selected exercise type is invalid."
            );

            return null;
        }
    }

    // Shows up to five numbered page controls centred
    // around the currently selected page.
    private List<Integer> buildPageNumbers(
            Page<?> historyPage
    ) {
        List<Integer> pageNumbers = new ArrayList<>();

        if (historyPage.getTotalPages() == 0) {
            return pageNumbers;
        }

        int start = Math.max(
                0,
                historyPage.getNumber() - 2
        );

        int end = Math.min(
                historyPage.getTotalPages() - 1,
                start + 4
        );

        start = Math.max(0, end - 4);

        for (
                int pageNumber = start;
                pageNumber <= end;
                pageNumber++
        ) {
            pageNumbers.add(pageNumber);
        }

        return pageNumbers;
    }

    private void addActiveQueryParameters(
            Model model,
            String search,
            ExerciseType selectedType,
            LocalDate startDate,
            LocalDate endDate,
            Double minCalories,
            Integer planId,
            int size,
            String sortField,
            String sortDir
    ) {
        model.addAttribute(
                "search",
                search == null ? "" : search.trim()
        );

        model.addAttribute(
                "selectedType",
                selectedType
        );

        model.addAttribute(
                "startDate",
                startDate
        );

        model.addAttribute(
                "endDate",
                endDate
        );

        model.addAttribute(
                "minCalories",
                minCalories
        );

        model.addAttribute(
                "planId",
                planId
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(WorkoutNotFoundException.class)
    public String handleWorkoutNotFound(
            WorkoutNotFoundException ex,
            Model model
    ) {
        model.addAttribute("message", ex.getMessage());

        return "workout-not-found";
    }
}