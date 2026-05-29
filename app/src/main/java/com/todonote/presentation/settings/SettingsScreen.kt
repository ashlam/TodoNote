package com.todonote.presentation.settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todonote.R
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToStatistics: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeSelector by remember { mutableStateOf(false) }
    var showDarkModeSelector by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showTagManager by remember { mutableStateOf(false) }

    // Show snackbar for backup/restore messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.backupMessage) {
        uiState.backupMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearBackupMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.theme)) },
                    leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showThemeSelector = true }
                )
            }

            if (showThemeSelector) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "选择主题",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            ThemeSelector(
                                selectedTheme = uiState.selectedTheme,
                                onThemeSelected = {
                                    viewModel.setTheme(it)
                                }
                            )
                        }
                    }
                }
            }

            item {
                val darkModeText = when (uiState.darkMode) {
                    true -> "深色"
                    false -> "浅色"
                    null -> "跟随系统"
                }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.dark_mode)) },
                    supportingContent = { Text(darkModeText) },
                    leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showDarkModeSelector = true }
                )
            }

            if (showDarkModeSelector) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "深色模式",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            DarkModeSelector(
                                selectedMode = uiState.darkMode,
                                onModeSelected = {
                                    viewModel.setDarkMode(it)
                                    showDarkModeSelector = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "番茄钟时长",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                DurationSlider(
                    label = "专注时长",
                    value = uiState.focusMinutes,
                    range = 10..60,
                    onValueChange = viewModel::setFocusMinutes
                )
            }
            item {
                DurationSlider(
                    label = "休息时长",
                    value = uiState.breakMinutes,
                    range = 1..30,
                    onValueChange = viewModel::setBreakMinutes
                )
            }
            item {
                DurationSlider(
                    label = "长休息时长",
                    value = uiState.longBreakMinutes,
                    range = 10..60,
                    onValueChange = viewModel::setLongBreakMinutes
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("统计与成就") },
                    leadingContent = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onNavigateToStatistics() }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("每日自动备份") },
                    supportingContent = { Text("每天自动导出 JSON 备份") },
                    leadingContent = { Icon(Icons.Filled.CloudSync, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoBackupEnabled,
                            onCheckedChange = { viewModel.setAutoBackup(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.backup_restore)) },
                    supportingContent = { Text("导出/导入所有数据") },
                    leadingContent = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.refreshBackupFiles()
                        showBackupDialog = true
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("标签管理") },
                    supportingContent = { Text("创建和管理标签颜色") },
                    leadingContent = { Icon(Icons.Filled.Label, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showTagManager = true }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about)) },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                )
            }
        }
    }

    // Tag Management Dialog
    if (showTagManager) {
        TagManagementDialog(onDismiss = { showTagManager = false })
    }

    // Backup/Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            backupFiles = uiState.backupFiles,
            isLoading = uiState.isLoading,
            onDismiss = { showBackupDialog = false },
            onExport = { viewModel.exportBackup() },
            onImport = { filePath -> viewModel.importBackup(filePath) }
        )
    }
}

@Composable
private fun BackupRestoreDialog(
    backupFiles: List<java.io.File>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("备份与恢复") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExport,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("导出备份")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "现有备份 (${backupFiles.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (backupFiles.isEmpty()) {
                    Text(
                        text = "暂无备份文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        backupFiles.take(5).forEach { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onImport(file.absolutePath) }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = dateFormat.format(Date(file.lastModified())),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { onImport(file.absolutePath) }) {
                                        Text("恢复")
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun DurationSlider(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$value 分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}

@Composable
private fun DarkModeSelector(
    selectedMode: Boolean?,
    onModeSelected: (Boolean?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val options = listOf(
            null to "跟随系统",
            false to "浅色模式",
            true to "深色模式"
        )
        options.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(text = label)
                if (selectedMode == mode) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
