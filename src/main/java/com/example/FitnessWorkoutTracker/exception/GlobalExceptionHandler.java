package com.example.FitnessWorkoutTracker.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            Model model
    ) {

        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(
            IllegalStateException ex,
            Model model
    ) {

        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception ex,
            Model model
    ) {

        model.addAttribute(
                "errorMessage",
                "Something went wrong. Please try again."
        );

        return "error";
    }
}
