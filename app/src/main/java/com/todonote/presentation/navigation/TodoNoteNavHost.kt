package com.todonote.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.todonote.presentation.calendar.CalendarScreen
import com.todonote.presentation.habits.HabitListScreen
import com.todonote.presentation.habits.HabitDetailScreen
import com.todonote.presentation.pomodoro.PomodoroScreen
import com.todonote.presentation.settings.SettingsScreen
import com.todonote.presentation.statistics.StatisticsScreen
import com.todonote.presentation.tasks.TaskListScreen
import com.todonote.presentation.tasks.TaskDetailScreen

@Composable
fun TodoNoteNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Tasks.route
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Tasks.route) {
                TaskListScreen(
                    onTaskClick = { taskId, listId ->
                        navController.navigate("task_detail/$taskId?listId=$listId")
                    },
                    onAddTask = { listId ->
                        navController.navigate("task_detail/0?listId=$listId")
                    }
                )
            }
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") { type = NavType.LongType },
                    navArgument("listId") { type = NavType.LongType; defaultValue = 1L }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId")
                val listId = backStackEntry.arguments?.getLong("listId") ?: 1L
                TaskDetailScreen(
                    taskId = if (taskId == 0L) null else taskId,
                    listId = listId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Habits.route) {
                HabitListScreen(
                    onHabitClick = { habitId ->
                        navController.navigate("habit_detail/$habitId")
                    }
                )
            }
            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(
                    navArgument("habitId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId")
                HabitDetailScreen(
                    habitId = habitId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Timer.route) { PomodoroScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToStatistics = {
                        navController.navigate(Screen.Statistics.route)
                    }
                )
            }
            composable(Screen.Statistics.route) { StatisticsScreen() }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Screen.bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(screen.icon(selected), contentDescription = null) },
                label = { Text(stringResource(screen.labelRes)) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun Screen.icon(selected: Boolean): ImageVector = when (this) {
    Screen.Tasks -> if (selected) Icons.Filled.List else Icons.Outlined.List
    Screen.Calendar -> if (selected) Icons.Filled.DateRange else Icons.Outlined.DateRange
    Screen.Habits -> if (selected) Icons.Filled.Refresh else Icons.Outlined.Refresh
    Screen.Timer -> if (selected) Icons.Filled.Timer else Icons.Outlined.Timer
    Screen.Settings -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
    Screen.TaskDetail -> Icons.Filled.List
    Screen.HabitDetail -> Icons.Filled.Refresh
    Screen.Statistics -> Icons.Filled.EmojiEvents
}
