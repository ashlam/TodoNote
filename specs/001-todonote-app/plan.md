# 实施方案：TodoNote Android 应用

**分支**: `001-todonote-app` | **日期**: 2026-05-26 | **规格文档**: [spec.md](spec.md)

**输入**: 来自 `specs/001-todonote-app/spec.md` 的功能规格说明

## 概述

构建 TodoNote —— 一个完全离线的 Android 待办清单、习惯打卡和番茄钟应用。采用 Kotlin + Jetpack Compose 构建 UI，使用 Room（SQLite）进行本地数据持久化，遵循 MVVM Clean Architecture。不依赖 Google Play Services、Firebase 或网络权限，适合在国内应用商店分发。

## 技术背景

**语言/版本**: Kotlin 2.0+, Java 17（JVM 目标版本）

**UI 框架**: Jetpack Compose + Material Design 3（Material You）

**主要依赖**:
- Jetpack Compose BOM（UI 框架）
- Room（本地 SQLite 数据库）
- Kotlin Coroutines + Flow（异步操作）
- Koin（依赖注入 —— 轻量级，非 Google）
- Navigation Compose（屏幕导航）
- DataStore Preferences（键值存储，用于设置）
- Coil（图标/主题的图片加载）

**存储方案**: Room（SQLite）用于结构化数据；DataStore 用于用户偏好设置

**架构模式**: MVVM + Clean Architecture（数据层/领域层/表现层）

**测试工具**: JUnit 5、Kotlin Coroutines Test、Turbine（Flow 测试）、Compose UI Test、MockK

**目标平台**: Android 8.0+（API 26+）

**项目类型**: 移动应用（Android）

**性能目标**:
- 中端设备冷启动 < 3秒
- 1000+ 条项目的列表滚动达到 60fps
- 单次数据库写入 < 100毫秒
- APK 体积 < 90MB

**约束条件**:
- 不依赖 Google Play Services 或 Firebase
- 无需网络权限
- 完全离线运行
- 必须符合国内应用商店要求
- 支持中文

**规模/范围**: 单用户，纯本地。5000+ 任务，50+ 清单，20+ 习惯

## 宪法检查

*关卡：必须在第0阶段研究之前通过。*

1. **隐私优先**：零网络数据收集。所有数据保留在设备上。
2. **本地优先架构**：所有功能必须离线工作。任何功能都不需要互联网。
3. **不绑定 Google**：不得使用 Play Services、FCM 或 Firebase。
4. **Android 生态兼容**：使用可通过 Google Maven 获取但运行时不需要 Play Services 的 Jetpack 库。
5. **可测试性**：核心业务逻辑必须能够在没有 Android 框架的情况下进行单元测试。
6. **性能意识**：从一开始就考虑列表虚拟化、懒加载和高效的数据库查询。

## 项目结构

```
TodoNote/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/todonote/
│   │   │   │   ├── TodoNoteApp.kt              # Application 类
│   │   │   │   ├── MainActivity.kt              # 单 Activity
│   │   │   │   │
│   │   │   │   ├── data/                        # 数据层
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── TodoNoteDatabase.kt  # Room 数据库
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── ListDao.kt
│   │   │   │   │   │   │   ├── TaskDao.kt
│   │   │   │   │   │   │   ├── HabitDao.kt
│   │   │   │   │   │   │   ├── HabitCheckInDao.kt
│   │   │   │   │   │   │   ├── TagDao.kt
│   │   │   │   │   │   │   └── PomodoroSessionDao.kt
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   ├── ListEntity.kt
│   │   │   │   │   │   │   ├── TaskEntity.kt
│   │   │   │   │   │   │   ├── HabitEntity.kt
│   │   │   │   │   │   │   ├── HabitCheckInEntity.kt
│   │   │   │   │   │   │   ├── TagEntity.kt
│   │   │   │   │   │   │   ├── TaskTagCrossRef.kt
│   │   │   │   │   │   │   ├── AchievementEntity.kt
│   │   │   │   │   │   │   └── PomodoroSessionEntity.kt
│   │   │   │   │   │   ├── converter/
│   │   │   │   │   │   │   └── Converters.kt     # Room 类型转换器
│   │   │   │   │   │   └── backup/
│   │   │   │   │   │       └── BackupManager.kt   # JSON 导出/导入
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── ListRepository.kt
│   │   │   │   │       ├── TaskRepository.kt
│   │   │   │   │       ├── HabitRepository.kt
│   │   │   │   │       ├── TagRepository.kt
│   │   │   │   │       ├── PomodoroRepository.kt
│   │   │   │   │       └── AchievementRepository.kt
│   │   │   │   │
│   │   │   │   ├── domain/                      # 领域层
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── TaskList.kt
│   │   │   │   │   │   ├── Task.kt
│   │   │   │   │   │   ├── Habit.kt
│   │   │   │   │   │   ├── HabitCheckIn.kt
│   │   │   │   │   │   ├── Tag.kt
│   │   │   │   │   │   ├── Achievement.kt
│   │   │   │   │   │   └── PomodoroSession.kt
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── task/
│   │   │   │   │   │   │   ├── GetTasksUseCase.kt
│   │   │   │   │   │   │   ├── CreateTaskUseCase.kt
│   │   │   │   │   │   │   ├── UpdateTaskUseCase.kt
│   │   │   │   │   │   │   ├── DeleteTaskUseCase.kt
│   │   │   │   │   │   │   └── ToggleTaskUseCase.kt
│   │   │   │   │   │   ├── list/
│   │   │   │   │   │   │   ├── GetListsUseCase.kt
│   │   │   │   │   │   │   ├── CreateListUseCase.kt
│   │   │   │   │   │   │   └── DeleteListUseCase.kt
│   │   │   │   │   │   ├── habit/
│   │   │   │   │   │   │   ├── GetHabitsUseCase.kt
│   │   │   │   │   │   │   ├── CreateHabitUseCase.kt
│   │   │   │   │   │   │   ├── CheckInHabitUseCase.kt
│   │   │   │   │   │   │   └── CalculateStreakUseCase.kt
│   │   │   │   │   │   ├── pomodoro/
│   │   │   │   │   │   │   └── PomodoroSessionUseCase.kt
│   │   │   │   │   │   └── achievement/
│   │   │   │   │   │       └── UnlockAchievementUseCase.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── IListRepository.kt
│   │   │   │   │       ├── ITaskRepository.kt
│   │   │   │   │       ├── IHabitRepository.kt
│   │   │   │   │       ├── ITagRepository.kt
│   │   │   │   │       ├── IPomodoroRepository.kt
│   │   │   │   │       └── IAchievementRepository.kt
│   │   │   │   │
│   │   │   │   ├── presentation/                 # 表现层
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── TodoNoteNavHost.kt
│   │   │   │   │   │   └── Screen.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   └── ThemeManager.kt
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── TodoCheckbox.kt
│   │   │   │   │   │   │   ├── PriorityBadge.kt
│   │   │   │   │   │   │   ├── DatePickerButton.kt
│   │   │   │   │   │   │   ├── SearchBar.kt
│   │   │   │   │   │   │   └── EmptyState.kt
│   │   │   │   │   │   └── extensions/
│   │   │   │   │   │       └── DateExtensions.kt
│   │   │   │   │   ├── tasks/
│   │   │   │   │   │   ├── TaskListScreen.kt
│   │   │   │   │   │   ├── TaskListViewModel.kt
│   │   │   │   │   │   ├── TaskDetailScreen.kt
│   │   │   │   │   │   └── TaskDetailViewModel.kt
│   │   │   │   │   ├── calendar/
│   │   │   │   │   │   ├── CalendarScreen.kt
│   │   │   │   │   │   └── CalendarViewModel.kt
│   │   │   │   │   ├── habits/
│   │   │   │   │   │   ├── HabitListScreen.kt
│   │   │   │   │   │   ├── HabitListViewModel.kt
│   │   │   │   │   │   ├── HabitDetailScreen.kt
│   │   │   │   │   │   └── HabitDetailViewModel.kt
│   │   │   │   │   ├── pomodoro/
│   │   │   │   │   │   ├── PomodoroScreen.kt
│   │   │   │   │   │   └── PomodoroViewModel.kt
│   │   │   │   │   ├── statistics/
│   │   │   │   │   │   ├── StatisticsScreen.kt
│   │   │   │   │   │   ├── StatisticsViewModel.kt
│   │   │   │   │   │   └── AchievementBadge.kt
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       ├── ThemeSelector.kt
│   │   │   │   │       └── BackupRestoreScreen.kt
│   │   │   │   │
│   │   │   │   └── di/                          # 依赖注入
│   │   │   │       ├── AppModule.kt
│   │   │   │       ├── DatabaseModule.kt
│   │   │   │       ├── RepositoryModule.kt
│   │   │   │       └── UseCaseModule.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml              # 中文字符串
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-zh/
│   │   │   │   │   └── strings.xml              # 中文本地化
│   │   │   │   └── ...
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                                 # 单元测试
│   │       └── java/com/todonote/
│   │           ├── domain/usecase/
│   │           ├── data/repository/
│   │           └── presentation/viewmodel/
│   │
├── gradle/
│   └── libs.versions.toml                        # 版本目录
├── build.gradle.kts                              # 根构建文件
├── settings.gradle.kts
└── gradle.properties
```

## 第0阶段：研究与关键决策

### 数据库设计

**决策**：使用 Room（SQLite），共8张表

```sql
-- 核心表
ListEntity (id, title, description, color, icon, sortOrder, createdAt, updatedAt)
TaskEntity (id, listId 外键, title, notes, dueDate, priority, isCompleted, 
            completedAt, createdAt, updatedAt, sortOrder, reminderTime)
TagEntity (id, name, color, createdAt)
TaskTagCrossRef (taskId 外键, tagId 外键)  -- 多对多关系

-- 习惯追踪
HabitEntity (id, name, description, frequency, targetCount, color, icon, 
             createdAt, updatedAt, isArchived)
HabitCheckInEntity (id, habitId 外键, date, isComplete, note, createdAt)

-- 番茄钟与成就
PomodoroSessionEntity (id, taskId 外键 可空, startedAt, endedAt, 
                       duration, type, isCompleted)
AchievementEntity (id, name, description, iconRes, conditionType, conditionValue, unlockedAt)
```

### UI 架构

- **单 Activity** 配合 Jetpack Navigation Compose
- **底部导航栏** 5个标签页：任务、日历、习惯、计时器、设置
- **Material 3 设计系统**，支持动态主题
- **LazyColumn** 配合稳定键（stable keys），确保所有列表流畅滚动

### 通知策略

- 使用 Android 的 `NotificationManager` + `AlarmManager` 发送提醒
- 不使用 FCM（Firebase Cloud Messaging）—— 所有调度在本地完成
- 番茄钟使用 `ForegroundService` 配合持久通知

### 数据导出/备份

- 导出为 JSON 文件到设备存储
- 从 JSON 文件导入（带校验）
- 应用更新前通过 `BackupManager` 自动备份

## 第1阶段：设计产物

### 页面映射

| 页面 | 路由 | ViewModel | 说明 |
|------|------|-----------|------|
| 任务列表 | `/tasks` | TaskListViewModel | 主待办视图，含清单和任务 |
| 任务详情 | `/tasks/{id}` | TaskDetailViewModel | 编辑任务详情、备注、日期 |
| 日历 | `/calendar` | CalendarViewModel | 日历，日/周/月视图 |
| 习惯列表 | `/habits` | HabitListViewModel | 习惯列表，带打卡格子 |
| 习惯详情 | `/habits/{id}` | HabitDetailViewModel | 习惯详情和历史 |
| 番茄钟 | `/timer` | PomodoroViewModel | 番茄钟计时器，含会话记录 |
| 统计 | `/stats` | StatisticsViewModel | 图表和成就徽章 |
| 设置 | `/settings` | SettingsViewModel | 主题、备份、关于 |

### 导航图

```
NavHost
├── 底部导航栏
│   ├── 任务 (TaskListScreen)
│   │   └── 任务详情 (TaskDetailScreen)  — 嵌套导航
│   ├── 日历 (CalendarScreen)
│   ├── 习惯 (HabitListScreen)
│   │   └── 习惯详情 (HabitDetailScreen)  — 嵌套导航
│   ├── 计时器 (PomodoroScreen)
│   └── 设置 (SettingsScreen)
│       ├── 主题选择器
│       ├── 备份与恢复
│       └── 数据统计（也可从设置进入）
```

### 数据流

```
UI（Compose）
  ↕ StateFlow/SharedFlow
ViewModel
  ↕ Flow
UseCase（领域逻辑）
  ↕
Repository（领域层接口，数据层实现）
  ↕
DAO（Room）
  ↕
SQLite 数据库
```

## 快速开始指南

### 开发环境设置

```bash
# 前置要求
- Android Studio Hedgehog (2023.1.1+) 或更新版本
- JDK 17+
- Android SDK 34+
- Gradle 8.4+

# 克隆并构建
git clone <仓库地址>
cd TodoNote
./gradlew assembleDebug

# 运行测试
./gradlew test

# 构建发布版 APK
./gradlew assembleRelease
```

### 关键构建配置

```kotlin
// build.gradle.kts (app)
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 26  // Android 8.0
        targetSdk = 34
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    
    // 核心库
    implementation("androidx.core:core-ktx:1.13.1")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // 导航
    implementation("androidx.navigation:navigation-compose:2.8.0")
    
    // DI
    implementation("io.insert-koin:koin-android:4.0.0")
    implementation("io.insert-koin:koin-androidx-compose:4.0.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    
    // 测试
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("app.cash.turbine:turbine:1.1.0")
}
```

### 项目 Gradle 配置

使用 Gradle 版本目录（`gradle/libs.versions.toml`）集中管理依赖版本。