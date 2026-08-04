package com.example.FitnessWorkoutTracker.service;

import com.example.FitnessWorkoutTracker.dto.DashboardComparison;
import com.example.FitnessWorkoutTracker.dto.DashboardStats;
import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.repository.CompletedWorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests the calculations required by the Member 5 progress dashboard.
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CompletedWorkoutRepository completedWorkoutRepository;

    @Test
    void weeklyStatsCalculateAllRequiredValues() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        List<CompletedWorkout> workouts = List.of(
                completedWorkout(
                        ExerciseType.CARDIO,
                        30,
                        300.0
                ),
                completedWorkout(
                        ExerciseType.CARDIO,
                        20,
                        160.0
                ),
                completedWorkout(
                        ExerciseType.STRENGTH,
                        10,
                        60.0
                )
        );

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(workouts);

        DashboardStats stats =
                dashboardService.getCurrentWeekStats();

        assertEquals("This Week", stats.getPeriodLabel());
        assertEquals(3, stats.getCompletedWorkoutCount());
        assertEquals(520.0, stats.getTotalCalories(), 0.001);
        assertEquals(60, stats.getTotalMinutes());
        assertEquals(20.0, stats.getAverageDuration(), 0.001);

        assertEquals(
                ExerciseType.CARDIO,
                stats.getMostFrequentExerciseType()
        );

        assertEquals(
                "Cardio",
                stats.getMostFrequentExerciseTypeLabel()
        );

        assertTrue(stats.hasWorkouts());
    }

    @Test
    void weeklyStatsUseMondayThroughSunday() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of());

        dashboardService.getCurrentWeekStats();

        ArgumentCaptor<LocalDate> startDateCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        ArgumentCaptor<LocalDate> endDateCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        verify(completedWorkoutRepository)
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        startDateCaptor.capture(),
                        endDateCaptor.capture()
                );

        LocalDate startDate = startDateCaptor.getValue();
        LocalDate endDate = endDateCaptor.getValue();

        assertEquals(
                java.time.DayOfWeek.MONDAY,
                startDate.getDayOfWeek()
        );

        assertEquals(
                java.time.DayOfWeek.SUNDAY,
                endDate.getDayOfWeek()
        );

        assertEquals(6, endDate.toEpochDay() - startDate.toEpochDay());
    }

    @Test
    void monthlyStatsUseFirstAndLastDayOfCurrentMonth() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of());

        dashboardService.getCurrentMonthStats();

        ArgumentCaptor<LocalDate> startDateCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        ArgumentCaptor<LocalDate> endDateCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        verify(completedWorkoutRepository)
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        startDateCaptor.capture(),
                        endDateCaptor.capture()
                );

        LocalDate startDate = startDateCaptor.getValue();
        LocalDate endDate = endDateCaptor.getValue();

        assertEquals(1, startDate.getDayOfMonth());
        assertEquals(startDate.getMonth(), endDate.getMonth());

        assertEquals(
                startDate.lengthOfMonth(),
                endDate.getDayOfMonth()
        );
    }

    @Test
    void emptyPeriodReturnsSafeZeroValues() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of());

        DashboardStats stats =
                dashboardService.getCurrentWeekStats();

        assertEquals(0, stats.getCompletedWorkoutCount());
        assertEquals(0.0, stats.getTotalCalories(), 0.001);
        assertEquals(0, stats.getTotalMinutes());
        assertEquals(0.0, stats.getAverageDuration(), 0.001);

        assertNull(stats.getMostFrequentExerciseType());

        assertEquals(
                "No workout data",
                stats.getMostFrequentExerciseTypeLabel()
        );

        assertFalse(stats.hasWorkouts());
    }

    @Test
    void monthlyComparisonCalculatesDifferencesFromPreviousMonth() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        List<CompletedWorkout> currentMonthWorkouts = List.of(
                completedWorkout(
                        ExerciseType.STRENGTH,
                        40,
                        240.0
                ),
                completedWorkout(
                        ExerciseType.STRENGTH,
                        20,
                        120.0
                )
        );

        List<CompletedWorkout> previousMonthWorkouts = List.of(
                completedWorkout(
                        ExerciseType.CARDIO,
                        30,
                        180.0
                )
        );

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(
                        currentMonthWorkouts,
                        previousMonthWorkouts
                );

        DashboardComparison comparison =
                dashboardService.getComparison("month");

        assertEquals(
                "This Month",
                comparison.getCurrentPeriod().getPeriodLabel()
        );

        assertEquals(
                "Last Month",
                comparison.getPreviousPeriod().getPeriodLabel()
        );

        assertEquals(1, comparison.getWorkoutCountDifference());
        assertEquals(180.0, comparison.getCalorieDifference(), 0.001);
        assertEquals(30, comparison.getMinuteDifference());

        assertEquals(
                100.0,
                comparison.getWorkoutCountPercentageChange(),
                0.001
        );

        assertEquals(
                "increase",
                comparison.getWorkoutCountTrend()
        );
    }

    @Test
    void comparisonHandlesPreviousPeriodWithNoWorkouts() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        List<CompletedWorkout> currentWorkouts = List.of(
                completedWorkout(
                        ExerciseType.CARDIO,
                        30,
                        300.0
                )
        );

        when(completedWorkoutRepository
                .findByCompletedTrueAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                        any(LocalDate.class),
                        any(LocalDate.class)
                ))
                .thenReturn(
                        currentWorkouts,
                        List.of()
                );

        DashboardComparison comparison =
                dashboardService.getComparison("week");

        assertEquals(
                100.0,
                comparison.getWorkoutCountPercentageChange(),
                0.001
        );

        assertEquals(
                100.0,
                comparison.getCaloriePercentageChange(),
                0.001
        );

        assertEquals(
                100.0,
                comparison.getMinutePercentageChange(),
                0.001
        );

        assertEquals(
                100,
                comparison.getCurrentWorkoutBarPercentage()
        );

        assertEquals(
                0,
                comparison.getPreviousWorkoutBarPercentage()
        );
    }

    @Test
    void invalidViewDefaultsToWeeklyView() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        assertEquals(
                DashboardService.WEEK_VIEW,
                dashboardService.sanitizeView("invalid")
        );

        assertEquals(
                DashboardService.WEEK_VIEW,
                dashboardService.sanitizeView(null)
        );

        assertEquals(
                DashboardService.MONTH_VIEW,
                dashboardService.sanitizeView("MONTH")
        );
    }

    @Test
    void recentWorkoutsComeFromRepository() {
        DashboardService dashboardService =
                new DashboardService(completedWorkoutRepository);

        List<CompletedWorkout> recentWorkouts = List.of(
                completedWorkout(
                        ExerciseType.CARDIO,
                        30,
                        300.0
                ),
                completedWorkout(
                        ExerciseType.STRENGTH,
                        20,
                        120.0
                )
        );

        when(completedWorkoutRepository
                .findTop5ByCompletedTrueOrderByWorkoutDateDescIdDesc())
                .thenReturn(recentWorkouts);

        List<CompletedWorkout> result =
                dashboardService.getRecentWorkouts();

        assertEquals(recentWorkouts, result);

        verify(completedWorkoutRepository)
                .findTop5ByCompletedTrueOrderByWorkoutDateDescIdDesc();
    }

    private CompletedWorkout completedWorkout(
            ExerciseType exerciseType,
            int duration,
            double calories
    ) {
        Exercise exercise = new Exercise(
                exerciseType.name() + " Exercise",
                exerciseType,
                5.0
        );

        CompletedWorkout workout = new CompletedWorkout();
        workout.setExercise(exercise);
        workout.setWorkoutDate(LocalDate.now());
        workout.setDuration(duration);
        workout.setCalories(calories);
        workout.setCompleted(true);

        return workout;
    }
}