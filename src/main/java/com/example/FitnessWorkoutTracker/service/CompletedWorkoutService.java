package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.repository.CompletedWorkoutRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Contains the business logic for recording completed workouts
// and calculating calories burned.
@Service
public class CompletedWorkoutService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final Set<Integer> ALLOWED_PAGE_SIZES =
            Set.of(5, 10, 20, 50);

    // Maps URL sort values to actual CompletedWorkout entity properties.
    // This prevents invalid or unsafe sort fields from being passed to Spring Data.
    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "workoutDate", "workoutDate",
            "exerciseName", "exercise.name",
            "duration", "duration",
            "calories", "calories"
    );

    private final CompletedWorkoutRepository completedWorkoutRepository;

    public CompletedWorkoutService(
            CompletedWorkoutRepository completedWorkoutRepository
    ) {
        this.completedWorkoutRepository = completedWorkoutRepository;
    }

    // Return every completed workout stored in the database.
    public List<CompletedWorkout> getAllWorkouts() {
        return completedWorkoutRepository.findAll();
    }

    // Return one completed workout by ID.
    public Optional<CompletedWorkout> getWorkoutById(Integer id) {
        return completedWorkoutRepository.findById(id);
    }

    // Return all completed workouts recorded by one user.
    public List<CompletedWorkout> getWorkoutsByUser(User user) {
        return completedWorkoutRepository.findByUser(user);
    }

    // Return all completed workouts recorded from one workout plan.
    public List<CompletedWorkout> getWorkoutsByPlan(
            WorkoutPlan workoutPlan
    ) {
        return completedWorkoutRepository.findByWorkoutPlan(workoutPlan);
    }

    // Return all completed workouts within an inclusive date range.
    public List<CompletedWorkout> getWorkoutsBetween(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return completedWorkoutRepository.findByWorkoutDateBetween(
                startDate,
                endDate
        );
    }

    // Return all completed workouts for one user within a date range.
    public List<CompletedWorkout> getWorkoutsByUserBetween(
            User user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return completedWorkoutRepository.findByUserAndWorkoutDateBetween(
                user,
                startDate,
                endDate
        );
    }

    // Run the history search with optional filters, pagination, and sorting.
    //
    // If the requested page is outside the available range, the final
    // available page is returned instead of displaying a broken page.
    public Page<CompletedWorkout> searchHistory(
            String search,
            ExerciseType type,
            LocalDate startDate,
            LocalDate endDate,
            Double minCalories,
            Integer planId,
            int page,
            int size,
            String sortField,
            String sortDir
    ) {
        String normalizedSearch = normalizeSearch(search);
        String safeSortField = sanitizeSortField(sortField);
        String safeSortDir = sanitizeSortDirection(sortDir);

        int safePage = Math.max(page, 0);

        int safeSize = ALLOWED_PAGE_SIZES.contains(size)
                ? size
                : DEFAULT_PAGE_SIZE;

        Sort.Direction direction = "asc".equals(safeSortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        direction,
                        ALLOWED_SORT_FIELDS.get(safeSortField)
                )
        );

        Page<CompletedWorkout> result =
                completedWorkoutRepository.searchHistory(
                        normalizedSearch,
                        type,
                        startDate,
                        endDate,
                        minCalories,
                        planId,
                        pageable
                );

        // Handle a page number that is greater than the final page.
        if (result.getTotalPages() > 0
                && safePage >= result.getTotalPages()) {

            Pageable finalPage = PageRequest.of(
                    result.getTotalPages() - 1,
                    safeSize,
                    Sort.by(
                            direction,
                            ALLOWED_SORT_FIELDS.get(safeSortField)
                    )
            );

            return completedWorkoutRepository.searchHistory(
                    normalizedSearch,
                    type,
                    startDate,
                    endDate,
                    minCalories,
                    planId,
                    finalPage
            );
        }

        return result;
    }

    // Only allow the sort fields supported by the history page.
    public String sanitizeSortField(String sortField) {
        if (ALLOWED_SORT_FIELDS.containsKey(sortField)) {
            return sortField;
        }

        return "workoutDate";
    }

    // Only "asc" or "desc" are valid.
    public String sanitizeSortDirection(String sortDir) {
        if ("asc".equalsIgnoreCase(sortDir)) {
            return "asc";
        }

        return "desc";
    }

    // Convert blank search values into null so the repository query
    // knows that no search filter was selected.
    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    // Calories Burned =
    // Exercise Calories Per Minute × Completed Duration in Minutes
    public double calculateCalories(
            Exercise exercise,
            Integer durationMinutes
    ) {
        if (exercise == null
                || exercise.getCaloriesPerMinute() == null
                || durationMinutes == null) {
            return 0.0;
        }

        return exercise.getCaloriesPerMinute() * durationMinutes;
    }

    // Save a completed workout and recalculate its calorie value.
    public CompletedWorkout saveWorkout(
            CompletedWorkout completedWorkout
    ) {
        completedWorkout.setCalories(
                calculateCalories(
                        completedWorkout.getExercise(),
                        completedWorkout.getDuration()
                )
        );

        return completedWorkoutRepository.save(completedWorkout);
    }

    // Delete a completed workout by ID.
    public void deleteWorkout(Integer id) {
        completedWorkoutRepository.deleteById(id);
    }
}