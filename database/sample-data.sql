-- Fitness Workout Tracker
-- Optional sample data for demonstrations and testing.
--
-- Run this file only after:
-- 1. The fitness_tracker database has been created.
-- 2. The Spring Boot application has been started at least once,
--    allowing Hibernate to create the database tables.
--
-- This script can be run more than once. Marker notes and lookup
-- conditions prevent the same sample records from being duplicated.

USE fitness_tracker;

START TRANSACTION;

-- ---------------------------------------------------------
-- 1. Demo user
-- ---------------------------------------------------------

INSERT IGNORE INTO app_user (
    name,
    email
)
VALUES (
    'Demo User',
    'demo@fitness-tracker.local'
);

SET @demo_user_id = (
    SELECT id
    FROM app_user
    WHERE email = 'demo@fitness-tracker.local'
    ORDER BY id
    LIMIT 1
);

-- ---------------------------------------------------------
-- 2. Demo exercises
-- ---------------------------------------------------------

INSERT INTO exercise (
    name,
    type,
    calories_per_minute
)
SELECT
    'Running',
    'CARDIO',
    10.0
WHERE NOT EXISTS (
    SELECT 1
    FROM exercise
    WHERE name = 'Running'
);

INSERT INTO exercise (
    name,
    type,
    calories_per_minute
)
SELECT
    'Bench Press',
    'STRENGTH',
    6.0
WHERE NOT EXISTS (
    SELECT 1
    FROM exercise
    WHERE name = 'Bench Press'
);

INSERT INTO exercise (
    name,
    type,
    calories_per_minute
)
SELECT
    'Yoga',
    'FLEXIBILITY',
    3.0
WHERE NOT EXISTS (
    SELECT 1
    FROM exercise
    WHERE name = 'Yoga'
);

SET @running_id = (
    SELECT id
    FROM exercise
    WHERE name = 'Running'
    ORDER BY id
    LIMIT 1
);

SET @bench_press_id = (
    SELECT id
    FROM exercise
    WHERE name = 'Bench Press'
    ORDER BY id
    LIMIT 1
);

SET @yoga_id = (
    SELECT id
    FROM exercise
    WHERE name = 'Yoga'
    ORDER BY id
    LIMIT 1
);

-- ---------------------------------------------------------
-- 3. Demo workout plan
-- ---------------------------------------------------------

INSERT INTO workout_plan (
    user_id,
    name,
    description,
    goal,
    scheduled_day,
    created_date
)
SELECT
    @demo_user_id,
    'Demo Weekly Fitness Plan',
    'Sample plan used to demonstrate the application.',
    'Improve strength and cardiovascular fitness',
    'MONDAY',
    CURDATE()
WHERE NOT EXISTS (
    SELECT 1
    FROM workout_plan
    WHERE user_id = @demo_user_id
      AND name = 'Demo Weekly Fitness Plan'
);

SET @demo_plan_id = (
    SELECT id
    FROM workout_plan
    WHERE user_id = @demo_user_id
      AND name = 'Demo Weekly Fitness Plan'
    ORDER BY id
    LIMIT 1
);

-- ---------------------------------------------------------
-- 4. Demo workout-plan exercises
-- ---------------------------------------------------------

INSERT INTO workout_plan_item (
    plan_id,
    exercise_id,
    sets,
    reps,
    target_minutes
)
SELECT
    @demo_plan_id,
    @running_id,
    1,
    1,
    30
WHERE NOT EXISTS (
    SELECT 1
    FROM workout_plan_item
    WHERE plan_id = @demo_plan_id
      AND exercise_id = @running_id
);

INSERT INTO workout_plan_item (
    plan_id,
    exercise_id,
    sets,
    reps,
    target_minutes
)
SELECT
    @demo_plan_id,
    @bench_press_id,
    3,
    10,
    45
WHERE NOT EXISTS (
    SELECT 1
    FROM workout_plan_item
    WHERE plan_id = @demo_plan_id
      AND exercise_id = @bench_press_id
);

INSERT INTO workout_plan_item (
    plan_id,
    exercise_id,
    sets,
    reps,
    target_minutes
)
SELECT
    @demo_plan_id,
    @yoga_id,
    1,
    1,
    20
WHERE NOT EXISTS (
    SELECT 1
    FROM workout_plan_item
    WHERE plan_id = @demo_plan_id
      AND exercise_id = @yoga_id
);

-- ---------------------------------------------------------
-- 5. Relative dates used by the dashboard
-- ---------------------------------------------------------

-- Monday of the current week.
SET @current_week_start =
    DATE_SUB(
        CURDATE(),
        INTERVAL WEEKDAY(CURDATE()) DAY
    );

-- Monday of the previous week.
SET @previous_week_start =
    DATE_SUB(
        @current_week_start,
        INTERVAL 7 DAY
    );

-- First day of the current month.
SET @current_month_start =
    DATE_FORMAT(
        CURDATE(),
        '%Y-%m-01'
    );

-- Final day of the previous month.
SET @previous_month_end =
    DATE_SUB(
        @current_month_start,
        INTERVAL 1 DAY
    );

-- ---------------------------------------------------------
-- 6. Current-week completed workouts
-- ---------------------------------------------------------

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @running_id,
    CURDATE(),
    30,
    1,
    1,
    300.0,
    'DEMO: Current week running workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Current week running workout'
);

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @bench_press_id,
    @current_week_start,
    45,
    3,
    10,
    270.0,
    'DEMO: Current week strength workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Current week strength workout'
);

-- ---------------------------------------------------------
-- 7. Previous-week completed workouts
-- ---------------------------------------------------------

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @running_id,
    @previous_week_start,
    25,
    1,
    1,
    250.0,
    'DEMO: Previous week running workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Previous week running workout'
);

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @bench_press_id,
    DATE_ADD(
        @previous_week_start,
        INTERVAL 1 DAY
    ),
    40,
    3,
    8,
    240.0,
    'DEMO: Previous week strength workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Previous week strength workout'
);

-- ---------------------------------------------------------
-- 8. Additional current-month workout
-- ---------------------------------------------------------

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @yoga_id,
    @current_month_start,
    20,
    1,
    1,
    60.0,
    'DEMO: Current month flexibility workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Current month flexibility workout'
);

-- ---------------------------------------------------------
-- 9. Previous-month completed workout
-- ---------------------------------------------------------

INSERT INTO completed_workout (
    user_id,
    plan_id,
    exercise_id,
    workout_date,
    duration,
    sets,
    reps,
    calories,
    notes,
    completed
)
SELECT
    @demo_user_id,
    @demo_plan_id,
    @yoga_id,
    @previous_month_end,
    35,
    1,
    1,
    105.0,
    'DEMO: Previous month flexibility workout',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM completed_workout
    WHERE notes = 'DEMO: Previous month flexibility workout'
);

COMMIT;

-- After running this script, refresh:
-- http://localhost:8080/dashboard