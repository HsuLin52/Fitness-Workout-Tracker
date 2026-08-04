package com.example.FitnessWorkoutTracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

// Provides one shared and friendly error response for all controllers.
@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles completed workouts or related records that cannot be found.
    @ExceptionHandler(WorkoutNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWorkoutNotFound(
            WorkoutNotFoundException ex,
            HttpServletRequest request,
            Model model
    ) {
        addErrorDetails(
                model,
                HttpStatus.NOT_FOUND,
                "Record Not Found",
                ex.getMessage(),
                request
        );

        return "error";
    }

    // Handles invalid IDs, invalid relationships, and other bad input.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request,
            Model model
    ) {
        addErrorDetails(
                model,
                HttpStatus.BAD_REQUEST,
                "Unable to Complete Request",
                safeMessage(
                        ex.getMessage(),
                        "The submitted request was not valid."
                ),
                request
        );

        return "error";
    }

    // Handles an action that cannot be completed in the current state.
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request,
            Model model
    ) {
        addErrorDetails(
                model,
                HttpStatus.CONFLICT,
                "Action Could Not Be Completed",
                safeMessage(
                        ex.getMessage(),
                        "The requested action cannot be completed right now."
                ),
                request
        );

        return "error";
    }

    // Handles unexpected application problems without exposing
    // technical exception details to the user.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(
            Exception ex,
            HttpServletRequest request,
            Model model
    ) {
        addErrorDetails(
                model,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something Went Wrong",
                "An unexpected problem occurred. Please return to the "
                        + "dashboard and try again.",
                request
        );

        return "error";
    }

    private void addErrorDetails(
            Model model,
            HttpStatus status,
            String errorTitle,
            String errorMessage,
            HttpServletRequest request
    ) {
        model.addAttribute(
                "statusCode",
                status.value()
        );

        model.addAttribute(
                "errorTitle",
                errorTitle
        );

        model.addAttribute(
                "errorMessage",
                errorMessage
        );

        model.addAttribute(
                "requestPath",
                request == null
                        ? ""
                        : request.getRequestURI()
        );
    }

    private String safeMessage(
            String message,
            String defaultMessage
    ) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }

        return message;
    }
}