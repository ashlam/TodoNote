package com.todonote.data.local.backup

import android.content.Context
import androidx.room.withTransaction
import com.todonote.data.local.TodoNoteDatabase
import com.todonote.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Date

class BackupManager(
    private val context: Context,
    private val database: TodoNoteDatabase
) {

    suspend fun exportToJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("exportDate", System.currentTimeMillis())
            root.put("appName", "TodoNote")

            // Export lists
            val lists = database.listDao().getAllSync()
            root.put("lists", lists.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("description", it.description)
                    put("color", it.color)
                    put("icon", it.icon)
                    put("sortOrder", it.sortOrder)
                    put("createdAt", it.createdAt)
                    put("updatedAt", it.updatedAt)
                }
            })

            // Export tasks
            val tasks = database.taskDao().getAllSync()
            root.put("tasks", tasks.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("listId", it.listId)
                    put("title", it.title)
                    put("notes", it.notes)
                    put("dueDate", it.dueDate ?: JSONObject.NULL)
                    put("priority", it.priority)
                    put("isCompleted", it.isCompleted)
                    put("completedAt", it.completedAt ?: JSONObject.NULL)
                    put("sortOrder", it.sortOrder)
                    put("reminderTime", it.reminderTime ?: JSONObject.NULL)
                    put("createdAt", it.createdAt)
                    put("updatedAt", it.updatedAt)
                }
            })

            // Export tags
            val tags = database.tagDao().getAllSync()
            root.put("tags", tags.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("color", it.color)
                    put("createdAt", it.createdAt)
                }
            })

            // Export task-tag cross refs
            val crossRefs = database.taskTagCrossRefDao().getAllSync()
            root.put("taskTagCrossRefs", crossRefs.toJsonArray {
                JSONObject().apply {
                    put("taskId", it.taskId)
                    put("tagId", it.tagId)
                }
            })

            // Export habits
            val habits = database.habitDao().getAllSync()
            root.put("habits", habits.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("description", it.description)
                    put("frequency", it.frequency)
                    put("targetCount", it.targetCount)
                    put("color", it.color)
                    put("icon", it.icon)
                    put("currentStreak", it.currentStreak)
                    put("bestStreak", it.bestStreak)
                    put("createdAt", it.createdAt)
                    put("updatedAt", it.updatedAt)
                    put("isArchived", it.isArchived)
                }
            })

            // Export habit check-ins
            val checkIns = database.habitCheckInDao().getAllSync()
            root.put("habitCheckIns", checkIns.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("habitId", it.habitId)
                    put("date", it.date)
                    put("isComplete", it.isComplete)
                    put("note", it.note)
                    put("createdAt", it.createdAt)
                }
            })

            // Export pomodoro sessions
            val sessions = database.pomodoroSessionDao().getAllSync()
            root.put("pomodoroSessions", sessions.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("taskId", it.taskId ?: JSONObject.NULL)
                    put("startedAt", it.startedAt)
                    put("endedAt", it.endedAt ?: JSONObject.NULL)
                    put("duration", it.duration)
                    put("type", it.type)
                    put("isCompleted", it.isCompleted)
                }
            })

            // Export achievements
            val achievements = database.achievementDao().getAllSync()
            root.put("achievements", achievements.toJsonArray {
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("description", it.description)
                    put("iconRes", it.iconRes)
                    put("conditionType", it.conditionType)
                    put("conditionValue", it.conditionValue)
                    put("unlockedAt", it.unlockedAt ?: JSONObject.NULL)
                }
            })

            val fileName = "todonote_backup_${Date().time}.json"
            val dir = File(context.getExternalFilesDir(null), "backups")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeText(root.toString(2))

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(IllegalArgumentException("备份文件不存在"))
            }

            val jsonString = file.readText()
            val backup = JSONObject(jsonString)

            val version = backup.optInt("version", 0)
            if (version != 1) {
                return@withContext Result.failure(IllegalArgumentException("不支持的备份版本: $version"))
            }

            database.withTransaction {
                // Clear existing data (order matters due to foreign keys)
                database.taskTagCrossRefDao().deleteAll()
                database.pomodoroSessionDao().deleteAll()
                database.habitCheckInDao().deleteAll()
                database.achievementDao().deleteAll()
                database.habitDao().deleteAll()
                database.taskDao().deleteAll()
                database.tagDao().deleteAll()
                database.listDao().deleteAll()

                // Restore lists
                backup.optJSONArray("lists")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.listDao().insert(ListEntity(
                            id = obj.getLong("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            color = obj.getInt("color"),
                            icon = obj.optString("icon", ""),
                            sortOrder = obj.optInt("sortOrder", 0),
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt")
                        ))
                    }
                }

                // Restore tasks
                backup.optJSONArray("tasks")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.taskDao().insert(TaskEntity(
                            id = obj.getLong("id"),
                            listId = obj.getLong("listId"),
                            title = obj.getString("title"),
                            notes = obj.optString("notes", ""),
                            dueDate = obj.takeIf { it.has("dueDate") && !it.isNull("dueDate") }?.getLong("dueDate"),
                            priority = obj.optInt("priority", TaskEntity.Priority.MEDIUM),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            completedAt = obj.takeIf { it.has("completedAt") && !it.isNull("completedAt") }?.getLong("completedAt"),
                            sortOrder = obj.optInt("sortOrder", 0),
                            reminderTime = obj.takeIf { it.has("reminderTime") && !it.isNull("reminderTime") }?.getLong("reminderTime"),
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt")
                        ))
                    }
                }

                // Restore tags
                backup.optJSONArray("tags")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.tagDao().insert(TagEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            color = obj.getInt("color"),
                            createdAt = obj.getLong("createdAt")
                        ))
                    }
                }

                // Restore task-tag cross refs
                backup.optJSONArray("taskTagCrossRefs")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.taskTagCrossRefDao().insert(TaskTagCrossRef(
                            taskId = obj.getLong("taskId"),
                            tagId = obj.getLong("tagId")
                        ))
                    }
                }

                // Restore habits
                backup.optJSONArray("habits")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.habitDao().insert(HabitEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            frequency = obj.optInt("frequency", HabitEntity.Frequency.DAILY),
                            targetCount = obj.optInt("targetCount", 1),
                            color = obj.getInt("color"),
                            icon = obj.optString("icon", ""),
                            currentStreak = obj.optInt("currentStreak", 0),
                            bestStreak = obj.optInt("bestStreak", 0),
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt"),
                            isArchived = obj.optBoolean("isArchived", false)
                        ))
                    }
                }

                // Restore habit check-ins
                backup.optJSONArray("habitCheckIns")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.habitCheckInDao().insert(HabitCheckInEntity(
                            id = obj.getLong("id"),
                            habitId = obj.getLong("habitId"),
                            date = obj.getLong("date"),
                            isComplete = obj.optBoolean("isComplete", false),
                            note = obj.optString("note", ""),
                            createdAt = obj.getLong("createdAt")
                        ))
                    }
                }

                // Restore pomodoro sessions
                backup.optJSONArray("pomodoroSessions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.pomodoroSessionDao().insert(PomodoroSessionEntity(
                            id = obj.getLong("id"),
                            taskId = obj.takeIf { it.has("taskId") && !it.isNull("taskId") }?.getLong("taskId"),
                            startedAt = obj.getLong("startedAt"),
                            endedAt = obj.takeIf { it.has("endedAt") && !it.isNull("endedAt") }?.getLong("endedAt"),
                            duration = obj.optInt("duration", 25 * 60),
                            type = obj.optInt("type", PomodoroSessionEntity.Type.FOCUS),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        ))
                    }
                }

                // Restore achievements
                backup.optJSONArray("achievements")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        database.achievementDao().insert(AchievementEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            description = obj.getString("description"),
                            iconRes = obj.optString("iconRes", ""),
                            conditionType = obj.getString("conditionType"),
                            conditionValue = obj.optInt("conditionValue", 0),
                            unlockedAt = obj.takeIf { it.has("unlockedAt") && !it.isNull("unlockedAt") }?.getLong("unlockedAt")
                        ))
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupFiles(): List<File> {
        val dir = File(context.getExternalFilesDir(null), "backups")
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private inline fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray {
        return JSONArray().apply {
            forEach { put(mapper(it)) }
        }
    }
}
