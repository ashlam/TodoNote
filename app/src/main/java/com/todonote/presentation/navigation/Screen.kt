package com.todonote.presentation.navigation

sealed class Screen(val route: String, val labelRes: Int) {
    data object Tasks : Screen("tasks", com.todonote.R.string.nav_tasks)
    data object Calendar : Screen("calendar", com.todonote.R.string.nav_calendar)
    data object Habits : Screen("habits", com.todonote.R.string.nav_habits)
    data object Timer : Screen("timer", com.todonote.R.string.nav_timer)
    data object Settings : Screen("settings", com.todonote.R.string.nav_settings)
    data object TaskDetail : Screen("task_detail/{taskId}?listId={listId}", com.todonote.R.string.nav_tasks)
    data object HabitDetail : Screen("habit_detail/{habitId}", com.todonote.R.string.nav_habits)
    data object Statistics : Screen("statistics", com.todonote.R.string.nav_settings)

    companion object {
        val bottomNavItems = listOf(Tasks, Calendar, Habits, Timer, Settings)
    }
}
