package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.dto.DashboardComparison;
import com.example.FitnessWorkoutTracker.dto.DashboardStats;
import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Tests the Member 5 progress-dashboard routes and model attributes.
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    private DashboardStats weeklyStats;
    private DashboardStats lastWeekStats;
    private DashboardStats monthlyStats;
    private DashboardStats lastMonthStats;

    @BeforeEach
    void setUpDashboardData() {
        LocalDate today = LocalDate.now();

        weeklyStats = new DashboardStats(
                "This Week",
                today.minusDays(2),
                today.plusDays(4),
                3,
                480.0,
                75,
                25.0,
                ExerciseType.CARDIO
        );

        lastWeekStats = new DashboardStats(
                "Last Week",
                today.minusDays(9),
                today.minusDays(3),
                2,
                300.0,
                50,
                25.0,
                ExerciseType.STRENGTH
        );

        monthlyStats = new DashboardStats(
                "This Month",
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth()),
                8,
                1250.0,
                210,
                26.25,
                ExerciseType.CARDIO
        );

        LocalDate previousMonth = today.minusMonths(1);

        lastMonthStats = new DashboardStats(
                "Last Month",
                previousMonth.withDayOfMonth(1),
                previousMonth.withDayOfMonth(
                        previousMonth.lengthOfMonth()
                ),
                6,
                900.0,
                160,
                26.67,
                ExerciseType.STRENGTH
        );

        when(dashboardService.getCurrentWeekStats())
                .thenReturn(weeklyStats);

        when(dashboardService.getCurrentMonthStats())
                .thenReturn(monthlyStats);

        when(dashboardService.getRecentWorkouts())
                .thenReturn(List.<CompletedWorkout>of());
    }

    @Test
    void rootPathRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void weeklyDashboardProvidesRequiredModelAttributes()
            throws Exception {

        DashboardComparison weeklyComparison =
                new DashboardComparison(
                        weeklyStats,
                        lastWeekStats
                );

        when(dashboardService.sanitizeView("week"))
                .thenReturn("week");

        when(dashboardService.getComparison("week"))
                .thenReturn(weeklyComparison);

        mockMvc.perform(
                        get("/dashboard")
                                .param("view", "week")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(
                        model().attribute(
                                "selectedView",
                                "week"
                        )
                )
                .andExpect(
                        model().attribute(
                                "weeklyStats",
                                weeklyStats
                        )
                )
                .andExpect(
                        model().attribute(
                                "monthlyStats",
                                monthlyStats
                        )
                )
                .andExpect(
                        model().attribute(
                                "comparison",
                                weeklyComparison
                        )
                )
                .andExpect(
                        model().attribute(
                                "currentStats",
                                weeklyStats
                        )
                )
                .andExpect(
                        model().attribute(
                                "previousStats",
                                lastWeekStats
                        )
                )
                .andExpect(
                        model().attribute(
                                "recentWorkouts",
                                List.of()
                        )
                );
    }

    @Test
    void monthlyDashboardUsesMonthlyComparison()
            throws Exception {

        DashboardComparison monthlyComparison =
                new DashboardComparison(
                        monthlyStats,
                        lastMonthStats
                );

        when(dashboardService.sanitizeView("month"))
                .thenReturn("month");

        when(dashboardService.getComparison("month"))
                .thenReturn(monthlyComparison);

        mockMvc.perform(
                        get("/dashboard")
                                .param("view", "month")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(
                        model().attribute(
                                "selectedView",
                                "month"
                        )
                )
                .andExpect(
                        model().attribute(
                                "currentStats",
                                monthlyStats
                        )
                )
                .andExpect(
                        model().attribute(
                                "previousStats",
                                lastMonthStats
                        )
                );
    }

    @Test
    void invalidViewSafelyDefaultsToWeeklyDashboard()
            throws Exception {

        DashboardComparison weeklyComparison =
                new DashboardComparison(
                        weeklyStats,
                        lastWeekStats
                );

        when(dashboardService.sanitizeView("invalid"))
                .thenReturn("week");

        when(dashboardService.getComparison("week"))
                .thenReturn(weeklyComparison);

        mockMvc.perform(
                        get("/dashboard")
                                .param("view", "invalid")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(
                        model().attribute(
                                "selectedView",
                                "week"
                        )
                )
                .andExpect(
                        model().attribute(
                                "currentStats",
                                weeklyStats
                        )
                );
    }
}