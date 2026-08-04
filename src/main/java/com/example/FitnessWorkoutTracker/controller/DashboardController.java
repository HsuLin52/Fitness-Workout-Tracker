package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.dto.DashboardComparison;
import com.example.FitnessWorkoutTracker.dto.DashboardStats;
import com.example.FitnessWorkoutTracker.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Handles the Member 5 progress dashboard.
@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    // Redirect the application home page to the progress dashboard.
    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/dashboard";
    }

    // Display either the weekly or monthly dashboard.
    //
    // Examples:
    // /dashboard?view=week
    // /dashboard?view=month
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(defaultValue = DashboardService.WEEK_VIEW)
            String view,
            Model model
    ) {
        String selectedView =
                dashboardService.sanitizeView(view);

        DashboardStats weeklyStats =
                dashboardService.getCurrentWeekStats();

        DashboardStats monthlyStats =
                dashboardService.getCurrentMonthStats();

        DashboardComparison comparison =
                dashboardService.getComparison(selectedView);

        model.addAttribute(
                "selectedView",
                selectedView
        );

        model.addAttribute(
                "weeklyStats",
                weeklyStats
        );

        model.addAttribute(
                "monthlyStats",
                monthlyStats
        );

        model.addAttribute(
                "comparison",
                comparison
        );

        model.addAttribute(
                "currentStats",
                comparison.getCurrentPeriod()
        );

        model.addAttribute(
                "previousStats",
                comparison.getPreviousPeriod()
        );

        model.addAttribute(
                "recentWorkouts",
                dashboardService.getRecentWorkouts()
        );

        return "dashboard";
    }
}