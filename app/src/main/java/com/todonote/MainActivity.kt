package com.todonote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.intPreferencesKey
import com.todonote.presentation.navigation.TodoNoteNavHost
import com.todonote.presentation.settings.dataStore
import com.todonote.presentation.theme.AppTheme
import com.todonote.presentation.theme.TodoNoteTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_CREATE_TASK = "com.todonote.action.CREATE_TASK"
        const val ACTION_OPEN_TIMER = "com.todonote.action.OPEN_TIMER"
        const val ACTION_OPEN_HABITS = "com.todonote.action.OPEN_HABITS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = resolveStartDestination(intent)

        val themeFlow = dataStore.data.map { prefs ->
            val ordinal = prefs[intPreferencesKey("selected_theme")] ?: 0
            AppTheme.entries.getOrElse(ordinal) { AppTheme.DEFAULT }
        }

        val darkModeFlow = dataStore.data.map { prefs ->
            when (prefs[intPreferencesKey("dark_mode")] ?: -1) {
                0 -> false
                1 -> true
                else -> null
            }
        }

        setContent {
            val appTheme by themeFlow.collectAsState(initial = AppTheme.DEFAULT)
            val darkMode by darkModeFlow.collectAsState(initial = null)

            val isDarkTheme = when (darkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            TodoNoteTheme(
                appTheme = appTheme,
                darkTheme = isDarkTheme
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoNoteNavHost(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun resolveStartDestination(intent: Intent): String {
        return when (intent.action) {
            ACTION_CREATE_TASK -> "task_detail/0?listId=1"
            ACTION_OPEN_TIMER -> "timer"
            ACTION_OPEN_HABITS -> "habits"
            else -> "tasks"
        }
    }
}
