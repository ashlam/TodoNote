package com.todonote.presentation.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.data.local.backup.BackupManager
import com.todonote.data.local.backup.BackupScheduler
import com.todonote.presentation.theme.AppTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.DEFAULT,
    val darkMode: Boolean? = null,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val autoBackupEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val backupMessage: String? = null,
    val backupFiles: List<java.io.File> = emptyList()
)

class SettingsViewModel(
    private val context: Context,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val THEME_KEY = intPreferencesKey("selected_theme")
    private val DARK_MODE_KEY = intPreferencesKey("dark_mode")
    private val FOCUS_MINUTES_KEY = intPreferencesKey("focus_minutes")
    private val BREAK_MINUTES_KEY = intPreferencesKey("break_minutes")
    private val LONG_BREAK_MINUTES_KEY = intPreferencesKey("long_break_minutes")
    private val AUTO_BACKUP_KEY = booleanPreferencesKey("auto_backup")

    init {
        viewModelScope.launch {
            context.dataStore.data.map { prefs ->
                val themeOrdinal = prefs[THEME_KEY] ?: 0
                val darkModeInt = prefs[DARK_MODE_KEY] ?: -1
                SettingsUiState(
                    selectedTheme = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.DEFAULT },
                    darkMode = when (darkModeInt) {
                        0 -> false
                        1 -> true
                        else -> null
                    },
                    focusMinutes = prefs[FOCUS_MINUTES_KEY] ?: 25,
                    breakMinutes = prefs[BREAK_MINUTES_KEY] ?: 5,
                    longBreakMinutes = prefs[LONG_BREAK_MINUTES_KEY] ?: 15,
                    autoBackupEnabled = prefs[AUTO_BACKUP_KEY] ?: false,
                    backupFiles = backupManager.getBackupFiles()
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[THEME_KEY] = theme.ordinal
            }
        }
    }

    fun setDarkMode(darkMode: Boolean?) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[DARK_MODE_KEY] = when (darkMode) {
                    false -> 0
                    true -> 1
                    null -> -1
                }
            }
        }
    }

    fun setFocusMinutes(minutes: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[FOCUS_MINUTES_KEY] = minutes.coerceIn(1, 60)
            }
        }
    }

    fun setBreakMinutes(minutes: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[BREAK_MINUTES_KEY] = minutes.coerceIn(1, 30)
            }
        }
    }

    fun setLongBreakMinutes(minutes: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[LONG_BREAK_MINUTES_KEY] = minutes.coerceIn(1, 60)
            }
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[AUTO_BACKUP_KEY] = enabled
            }
            if (enabled) {
                BackupScheduler.schedule(context)
            } else {
                BackupScheduler.cancel(context)
            }
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = backupManager.exportToJson()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    backupMessage = result.fold(
                        onSuccess = { path -> "备份成功: ${path.substringAfterLast('/')}" },
                        onFailure = { e -> "备份失败: ${e.message}" }
                    ),
                    backupFiles = backupManager.getBackupFiles()
                )
            }
        }
    }

    fun importBackup(filePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = backupManager.importFromJson(filePath)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    backupMessage = result.fold(
                        onSuccess = { "恢复成功" },
                        onFailure = { e -> "恢复失败: ${e.message}" }
                    )
                )
            }
        }
    }

    fun clearBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    fun refreshBackupFiles() {
        _uiState.update { it.copy(backupFiles = backupManager.getBackupFiles()) }
    }
}
