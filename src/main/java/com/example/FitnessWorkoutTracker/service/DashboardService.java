package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.dto.DashboardComparison;
import com.example.FitnessWorkoutTracker.dto.DashboardStats;
import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.repository.CompletedWorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Calculates progress information for the Member 5 dashboard.
@Service
public class DashboardService {

    public static final String WEEK_VIEW = "week";
    public static final String MONTH_VIEW = "month";

    private final CompletedWorkoutRepository completedWorkoutRepository;

    public DashboardService(
            CompletedWorkoutRepository completedWorkoutRepository
    ) {
        this.completedWorkoutRepository = completedWorkoutRepository;
    }

    // Returns the current week's dashboard statistics.
    //
    // A week begins on Monday and ends on Sunday.
    public DashboardStats getCurrentWeekStats() {
        LocalDate today = LocalDate.now();

        LocalDate startDate = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate endDate = today.with(
                TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
        );

        return calculateStats(
                "This Week",
                startDate,
                endDate
        );
    }

    // Returns the current month's dashboard statistics.
    public DashboardStats getCurrentMonthStats() {
        LocalDate today = LocalDate.now();

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(
                today.lengthOfMonth()
        );

        return calculateStats(
                "This Month",
                startDate,
                endDate
        );
    }

    // Returns a comparison for either:
    // - This week compared with last week
    // - This month compared with last month
    //
    // An invalid view safely defaults to the weekly comparison.
    public DashboardComparison getComparison(String view) {
        if (MONTH_VIEW.equalsIgnoreCase(view)) {
            return getMonthlyComparison();
        }

        return getWeeklyComparison();
    }

    // Returns the five most recent completed workouts.
    public List<CompletedWorkout> getRecentWorkouts() {
        return completedWorkoutRepository
                .findTop5ByCompletedTrueOrderByWorkoutDateDescIdDesc();
    }

    // Converts an incoming dashboard view into a safe URL value.
    public String sanitizeView(String view) {
        if (MONTH_VIEW.equalsIgnoreCase(view)) {
            return MONTH_VIEW;
        }

        return WEEK_VIEW;
    }

    private DashboardComparison getWeeklyComparison() {
        LocalDate today = LocalDate.now();

        LocalDate currentStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate currentEnd = currentStart.plusDays(6);

        LocalDate previousStart = currentStart.minusWeeks(1);
        LocalDate previousEnd = previousStart.plusDays(6);

        DashboardStats currentPeriod = calculateStats(
                "This Week",
                currentStart,
                currentEnd
        );

        DashboardStats previousPeriod = calculateStats(
                "Last Week",
                previousStart,
                previousEnd
        );

        return new DashboardComparison(
                currentPeriod,
                previousPeriod
        );
    }

    private DashboardComparison getMonthlyComparison() {
        LocalDate today = LocalDate.now();

        LocalDate currentStart = today.withDayOfMonth(1);
        LocalDate currentEnd = today.withDayOfMonth(
                today.lengthOfMonth()
        );

        LocalDate previousMonth = today.minusMonths(1);

        LocalDate previousStart =
                previousMonth.withDayOfMonth(1);

        LocalDate previousEnd =
                previousMonth.withDayOfMonth(
                        previousMonth.lengthOfMonth()
                );

        DashboardStats currentPeriod = calculateStats(
                "This Month",
                currentStart,
                currentEnd
        );

        DashboardStats previousPeriod = calculateStats(
                "Last Month",
                previousStart,
                previousEnd
        );

        return new DashboardComparison(
                currentPeriod,
                previousPeriod
        );
    }

    // Loads the completed workouts for one inclusive date range
    // and calculates all required dashboard values.
    private DashboardStats calculateStats(
            String periodLabel,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<CompletedWorkout> workouts =
                completedWorkoutRepository
                        .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                                startDate,
                                endDate
                        );

        long completedWorkoutCount = workouts.size();

        double totalCalories = 0.0;
        long totalMinutes = 0;

        Map<ExerciseType, Long> exerciseTypeCounts =
                new EnumMap<>(ExerciseType.class);

        for (CompletedWorkout workout : workouts) {
            if (workout.getCalories() != null) {
                totalCalories += workout.getCalories();
            }

            if (workout.getDuration() != null) {
                totalMinutes += workout.getDuration();
            }

            if (workout.getExercise() != null
                    && workout.getExercise().getType() != null) {

                ExerciseType exerciseType =
                        workout.getExercise().getType();

                exerciseTypeCounts.merge(
                        exerciseType,
                        1L,
                        Long::sum
                );
            }
        }

        double averageDuration =
                completedWorkoutCount == 0
                        ? 0.0
                        : (double) totalMinutes
                        / completedWorkoutCount;

        ExerciseType mostFrequentExerciseType =
                findMostFrequentExerciseType(
                        exerciseTypeCounts
                );

        return new DashboardStats(
                periodLabel,
                startDate,
                endDate,
                completedWorkoutCount,
                totalCalories,
                totalMinutes,
                averageDuration,
                mostFrequentExerciseType
        );
    }

    // Finds the exercise type with the highest number of workouts.
    //
    // Iterating through ExerciseType.values() also makes ties
    // deterministic instead of returning a random result.
    private ExerciseType findMostFrequentExerciseType(
            Map<ExerciseType, Long> exerciseTypeCounts
    ) {
        ExerciseType mostFrequentType = null;
        long highestCount = 0;

        for (ExerciseType exerciseType : ExerciseType.values()) {
            long count = exerciseTypeCounts.getOrDefault(
                    exerciseType,
                    0L
            );

            if (count > highestCount) {
                highestCount = count;
                mostFrequentType = exerciseType;
            }
        }

        return mostFrequentType;
    }
}