package com.example.FitnessWorkoutTracker.controller;

import com.example.FitnessWorkoutTracker.model.CompletedWorkout;
import com.example.FitnessWorkoutTracker.model.Exercise;
import com.example.FitnessWorkoutTracker.model.ExerciseType;
import com.example.FitnessWorkoutTracker.model.User;
import com.example.FitnessWorkoutTracker.model.WorkoutPlan;
import com.example.FitnessWorkoutTracker.model.WorkoutPlanItem;
import com.example.FitnessWorkoutTracker.repository.ExerciseRepository;
import com.example.FitnessWorkoutTracker.repository.UserRepository;
import com.example.FitnessWorkoutTracker.service.CompletedWorkoutService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanItemService;
import com.example.FitnessWorkoutTracker.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Tests the /workouts routes, including recording a workout from a saved plan and the
// friendly validation for missing/mismatched users, exercises, and plans.
@WebMvcTest(CompletedWorkoutController.class)
class CompletedWorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompletedWorkoutService completedWorkoutService;

    @MockitoBean
    private WorkoutPlanService workoutPlanService;

    @MockitoBean
    private WorkoutPlanItemService workoutPlanItemService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ExerciseRepository exerciseRepository;

    private User user(int id) {
        User user = new User("Alex", "alex" + id + "@example.com");
        user.setId(id);
        return user;
    }

    private Exercise exercise(int id) {
        Exercise exercise = new Exercise("Running", ExerciseType.CARDIO, 8.0);
        exercise.setId(id);
        return exercise;
    }

    private WorkoutPlan plan(int id, User owner) {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(id);
        plan.setName("Cardio Plan");
        plan.setUser(owner);
        return plan;
    }

    @Test
    void listWorkoutsReturnsWorkoutListView() throws Exception {
        when(completedWorkoutService.getAllWorkouts()).thenReturn(List.of());

        mockMvc.perform(get("/workouts"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-list"));
    }

    @Test
    void showAddFormReturnsWorkoutFormView() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());
        when(exerciseRepository.findAll()).thenReturn(List.of());
        when(workoutPlanService.getAllPlans()).thenReturn(List.of());

        mockMvc.perform(get("/workouts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"));
    }

    @Test
    void showAddFormWithItemIdPrefillsUserExerciseSetsRepsAndDuration() throws Exception {
        User owner = user(1);
        WorkoutPlan plan = plan(1, owner);
        Exercise exercise = exercise(1);

        WorkoutPlanItem item = new WorkoutPlanItem();
        item.setId(5);
        item.setWorkoutPlan(plan);
        item.setExercise(exercise);
        item.setSets(4);
        item.setReps(12);
        item.setTargetMinutes(25);

        when(workoutPlanItemService.getItemById(5)).thenReturn(Optional.of(item));
        when(userRepository.findAll()).thenReturn(List.of());
        when(exerciseRepository.findAll()).thenReturn(List.of());
        when(workoutPlanService.getAllPlans()).thenReturn(List.of());

        mockMvc.perform(get("/workouts/add").param("itemId", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"))
                .andExpect(model().attribute("completedWorkout",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.hasProperty("user", org.hamcrest.Matchers.is(owner)),
                                org.hamcrest.Matchers.hasProperty("exercise", org.hamcrest.Matchers.is(exercise)),
                                org.hamcrest.Matchers.hasProperty("workoutPlan", org.hamcrest.Matchers.is(plan)),
                                org.hamcrest.Matchers.hasProperty("sets", org.hamcrest.Matchers.is(4)),
                                org.hamcrest.Matchers.hasProperty("reps", org.hamcrest.Matchers.is(12)),
                                org.hamcrest.Matchers.hasProperty("duration", org.hamcrest.Matchers.is(25))
                        )));
    }

    @Test
    void addWorkoutWithoutPlanSavesAndRedirectsToDetails() throws Exception {
        User owner = user(1);
        Exercise exercise = exercise(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(1)).thenReturn(Optional.of(exercise));
        when(completedWorkoutService.saveWorkout(any(CompletedWorkout.class)))
                .thenAnswer(invocation -> {
                    CompletedWorkout workout = invocation.getArgument(0);
                    workout.setId(42);
                    workout.setCalories(80.0);
                    return workout;
                });

        mockMvc.perform(post("/workouts/add")
                        .param("userId", "1")
                        .param("exerciseId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workouts/42"));
    }

    @Test
    void addWorkoutWithoutSelectingAUserShowsFriendlyError() throws Exception {
        mockMvc.perform(post("/workouts/add")
                        .param("exerciseId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"))
                .andExpect(model().attribute("formError", "Please select a user."));
    }

    @Test
    void addWorkoutWithExerciseNotInSelectedPlanShowsFriendlyError() throws Exception {
        User owner = user(1);
        Exercise exercise = exercise(1);
        WorkoutPlan plan = plan(1, owner);

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(1)).thenReturn(Optional.of(exercise));
        when(workoutPlanService.getPlanById(1)).thenReturn(Optional.of(plan));
        when(workoutPlanItemService.getItemsByPlan(plan)).thenReturn(List.of());

        mockMvc.perform(post("/workouts/add")
                        .param("userId", "1")
                        .param("exerciseId", "1")
                        .param("planId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"))
                .andExpect(model().attribute("formError", "Selected exercise is not part of the selected workout plan."));
    }

    @Test
    void addWorkoutWithPlanNotOwnedByUserShowsFriendlyError() throws Exception {
        User owner = user(1);
        User otherUser = user(2);
        Exercise exercise = exercise(1);
        WorkoutPlan plan = plan(1, owner);

        when(userRepository.findById(2)).thenReturn(Optional.of(otherUser));
        when(exerciseRepository.findById(1)).thenReturn(Optional.of(exercise));
        when(workoutPlanService.getPlanById(1)).thenReturn(Optional.of(plan));

        mockMvc.perform(post("/workouts/add")
                        .param("userId", "2")
                        .param("exerciseId", "1")
                        .param("planId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"))
                .andExpect(model().attribute("formError", "Selected workout plan does not belong to the selected user."));
    }

    @Test
    void viewWorkoutReturnsDetailsView() throws Exception {
        CompletedWorkout workout = new CompletedWorkout();
        workout.setId(9);
        workout.setUser(user(1));
        workout.setExercise(exercise(1));

        when(completedWorkoutService.getWorkoutById(9)).thenReturn(Optional.of(workout));

        mockMvc.perform(get("/workouts/9"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-details"));
    }

    @Test
    void viewWorkoutNotFoundShowsFriendlyNotFoundPage() throws Exception {
        when(completedWorkoutService.getWorkoutById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/workouts/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("workout-not-found"))
                .andExpect(model().attribute("message", "Completed workout not found with ID: 999"));
    }

    @Test
    void updateWorkoutForMissingRecordShowsFriendlyError() throws Exception {
        when(completedWorkoutService.getWorkoutById(123)).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of());
        when(exerciseRepository.findAll()).thenReturn(List.of());
        when(workoutPlanService.getAllPlans()).thenReturn(List.of());

        mockMvc.perform(post("/workouts/edit")
                        .param("id", "123")
                        .param("userId", "1")
                        .param("exerciseId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("workout-form"))
                .andExpect(model().attribute("formError", "The workout you are trying to update could not be found."));
    }

    @Test
    void updateWorkoutSavesAndRedirectsWhenExistingRecordIsLoaded() throws Exception {
        User owner = user(1);
        Exercise exercise = exercise(1);
        CompletedWorkout existing = new CompletedWorkout();
        existing.setId(7);

        when(completedWorkoutService.getWorkoutById(7)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(1)).thenReturn(Optional.of(exercise));
        when(completedWorkoutService.saveWorkout(any(CompletedWorkout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/workouts/edit")
                        .param("id", "7")
                        .param("userId", "1")
                        .param("exerciseId", "1")
                        .param("workoutDate", LocalDate.now().toString())
                        .param("duration", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workouts/7"));
    }

    @Test
    void deleteWorkoutRedirectsToList() throws Exception {
        CompletedWorkout workout = new CompletedWorkout();
        workout.setId(4);
        when(completedWorkoutService.getWorkoutById(4)).thenReturn(Optional.of(workout));

        mockMvc.perform(post("/workouts/delete/4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workouts"));
    }

    @Test
    void deleteWorkoutNotFoundShowsFriendlyNotFoundPage() throws Exception {
        when(completedWorkoutService.getWorkoutById(404)).thenReturn(Optional.empty());

        mockMvc.perform(post("/workouts/delete/404"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("workout-not-found"))
                .andExpect(model().attribute("message", "Completed workout not found with ID: 404"));
    }
}
