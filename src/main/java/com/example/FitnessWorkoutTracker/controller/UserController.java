package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Handles user-management pages.
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Displays every user currently stored in the database.
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "false")
            boolean created,
            Model model
    ) {
        model.addAttribute(
                "users",
                userService.getAllUsers()
        );

        model.addAttribute(
                "created",
                created
        );

        return "user-list";
    }

    // Displays the form for creating a new user.
    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute(
                "user",
                new User()
        );

        return "user-form";
    }

    // Validates and saves the submitted user.
    @PostMapping("/add")
    public String addUser(
            @Valid
            @ModelAttribute("user")
            User user,
            BindingResult result
    ) {
        // Add a clear field error when the email is already used.
        if (!result.hasFieldErrors("email")
                && userService.emailExists(user.getEmail())) {

            result.rejectValue(
                    "email",
                    "duplicate",
                    "A user with this email already exists"
            );
        }

        if (result.hasErrors()) {
            return "user-form";
        }

        userService.saveUser(user);

        return "redirect:/users?created=true";
    }
}