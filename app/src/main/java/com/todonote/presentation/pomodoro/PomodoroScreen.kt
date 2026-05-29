package com.todonote.presentation.pomodoro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todonote.R
import com.todonote.domain.model.PomodoroSession
import com.todonote.domain.model.SessionType
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.timerState, uiState.sessionType) {
        if (uiState.timerState is TimerState.Running) {
            val typeName = when (uiState.sessionType) {
                SessionType.FOCUS -> "专注"
                SessionType.BREAK -> "休息"
                SessionType.LONG_BREAK -> "长休息"
            }
            PomodoroService.startService(context, typeName, uiState.timeRemaining)
        } else if (uiState.timerState is TimerState.Idle) {
            PomodoroService.stopService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_timer)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Session type label
            Text(
                text = when (uiState.sessionType) {
                    SessionType.FOCUS -> "专注"
                    SessionType.BREAK -> "休息"
                    SessionType.LONG_BREAK -> "长休息"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Circular timer
            CircularTimer(
                totalSeconds = uiState.totalDuration,
                remainingSeconds = uiState.timeRemaining,
                color = when (uiState.sessionType) {
                    SessionType.FOCUS -> MaterialTheme.colorScheme.primary
                    SessionType.BREAK -> Color(0xFF4CAF50)
                    SessionType.LONG_BREAK -> Color(0xFF2196F3)
                },
                modifier = Modifier.size(260.dp)
            )

            // Timer text
            Text(
                text = formatTime(uiState.timeRemaining),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            // Today stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                StatItem(label = "今日专注", value = "${uiState.todaySessionCount} 次")
                StatItem(label = "专注时长", value = "${uiState.todayFocusMinutes} 分钟")
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                FilledTonalIconButton(
                    onClick = viewModel::resetTimer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "重置")
                }

                // Start/Pause button
                Button(
                    onClick = {
                        when (uiState.timerState) {
                            is TimerState.Running -> viewModel.pauseTimer()
                            else -> viewModel.startTimer()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.timerState is TimerState.Running)
                            Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (uiState.timerState is TimerState.Running) "暂停" else "开始",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Skip button
                FilledTonalIconButton(
                    onClick = viewModel::skipSession,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "跳过")
                }
            }

            // Session indicator dots
            if (uiState.sessionType == SessionType.FOCUS || uiState.completedSessions > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(PomodoroViewModel.SESSIONS_BEFORE_LONG_BREAK) { index ->
                        Box(
                            modifier = Modifier.size(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val isCompleted = index < uiState.completedSessions % PomodoroViewModel.SESSIONS_BEFORE_LONG_BREAK ||
                                    (uiState.completedSessions > 0 && uiState.completedSessions % PomodoroViewModel.SESSIONS_BEFORE_LONG_BREAK == 0 && index == 0 && uiState.sessionType != SessionType.FOCUS)
                            val isCurrent = index == uiState.completedSessions % PomodoroViewModel.SESSIONS_BEFORE_LONG_BREAK && uiState.sessionType == SessionType.FOCUS
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 10.dp else 8.dp)
                                    .background(
                                        color = when {
                                            isCompleted -> MaterialTheme.colorScheme.primary
                                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // Session history
            if (uiState.recentSessions.isNotEmpty()) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                uiState.recentSessions.take(10).forEach { session ->
                    SessionHistoryItem(session = session)
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryItem(session: PomodoroSession) {
    val sdf = remember { SimpleDateFormat("MM-dd HH:mm", Locale.CHINA) }
    val typeText = when (session.type) {
        SessionType.FOCUS -> "专注"
        SessionType.BREAK -> "休息"
        SessionType.LONG_BREAK -> "长休息"
    }
    val statusColor = when {
        session.isCompleted && session.type == SessionType.FOCUS -> MaterialTheme.colorScheme.primary
        session.isCompleted -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.outline
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$typeText · ${session.duration / 60} 分钟",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = sdf.format(Date(session.startedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(statusColor, CircleShape)
        )
    }
}

@Composable
private fun CircularTimer(
    totalSeconds: Int,
    remainingSeconds: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )
        val arcSize = Size(diameter, diameter)

        // Background track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
