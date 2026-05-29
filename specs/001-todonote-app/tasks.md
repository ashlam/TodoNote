# 任务清单：TodoNote Android 应用

**输入**: 来自 `specs/001-todonote-app/` 的设计文档

**前置依赖**: plan.md（必需）、spec.md（必需，含用户故事）

**格式**: `[ID] [P] [故事] 文件路径和说明`

## 阶段1：项目搭建（共享基础设施）

**目的**: 初始化 Android 项目，配置所有基础工具链

- [x] T001 使用 Kotlin DSL、版本目录和包名 `com.todonote` 创建 Android 项目结构
- [x] T002 配置 Gradle，添加所有依赖（Compose BOM、Room、Koin、Navigation、DataStore、Coroutines）
- [x] T003 搭建 Room 数据库，包含所有实体、DAO、类型转换器和数据库类，位于 `data/local/`
- [x] T004 实现 Koin DI 模块（AppModule、DatabaseModule、RepositoryModule、UseCaseModule、ViewModelModule），位于 `di/`
- [x] T005 配置 Material 3 主题系统，支持深色/浅色模式和8套配色方案，位于 `presentation/theme/`
- [x] T006 搭建 Navigation Compose 和底部导航栏（5个标签页），位于 `presentation/navigation/`
- [x] T007 创建通用 UI 组件（TodoCheckbox、PriorityBadge、SearchBar、EmptyState），位于 `presentation/common/components/`

**检查点**: 项目可编译，导航结构正常运行，数据库 schema 创建成功 ✅

---

## 阶段2：用户故事1 - 待办清单管理（优先级：P1）🎯 MVP

**目标**: 用户可以创建清单、添加任务、设置优先级/截止日期、追踪完成进度。

**独立测试**: 创建多个清单，添加不同优先级和截止日期的任务，标记完成，重启应用验证数据持久化。

### 用户故事1的实现

- [x] T008 [P] [US1] 实现 ListEntity、ListDao 和 ListRepository，位于 `data/local/`
- [x] T009 [P] [US1] 实现 TaskEntity、TaskDao 和 TaskRepository，位于 `data/local/`
- [x] T010 [P] [US1] 实现领域模型（TaskList、Task）和仓库接口，位于 `domain/`
- [x] T011 [P] [US1] 创建 GetListsUseCase、CreateListUseCase、DeleteListUseCase，位于 `domain/usecase/list/`
- [x] T012 [P] [US1] 创建 GetTasksUseCase、CreateTaskUseCase、UpdateTaskUseCase、DeleteTaskUseCase、ToggleTaskUseCase、SearchTasksUseCase，位于 `domain/usecase/task/`
- [x] T013 [US1] 构建 TaskListScreen，包含清单侧边栏、任务列表、进度指示器和搜索，位于 `presentation/tasks/TaskListScreen.kt`
- [x] T014 [US1] 实现 TaskListViewModel，管理清单和任务的状态，位于 `presentation/tasks/TaskListViewModel.kt`
- [x] T015 [US1] 构建 TaskDetailScreen，包含标题、备注、日期选择器、优先级选择器，位于 `presentation/tasks/TaskDetailScreen.kt`
- [x] T016 [US1] 实现 TaskDetailViewModel，用于任务的创建和编辑，位于 `presentation/tasks/TaskDetailViewModel.kt`
- [x] T017 [US1] 实现任务和清单的拖拽排序
- [x] T018 [US1] 使用 AlarmManager + NotificationManager 为带截止日期的任务添加提醒调度

**检查点**: 任务完整的增删改查功能正常运行，进度可见，数据本地持久化

---

## 阶段3：用户故事2 - 日历与时间线视图（优先级：P2）

**目标**: 用户可以在日/周/月日历视图中按日期查看任务。

**独立测试**: 创建不同截止日期的任务，浏览日历，验证任务出现在正确的日期上。

### 用户故事2的实现

- [x] T019 [P] [US2] 构建日历网格组件（月视图），位于 `presentation/calendar/CalendarScreen.kt`
- [x] T020 [P] [US2] 实现 CalendarViewModel，支持日期导航和任务聚合，位于 `presentation/calendar/CalendarViewModel.kt`
- [x] T021 [US2] 实现点击日历日期时显示当日任务列表
- [x] T022 [P] [US2] 在有任务的日期上添加视觉指示器（圆点/徽章）
- [x] T023 [US2] 支持在日历视图中长按日期快速创建任务

**检查点**: 日历在所有视图模式下正确显示任务，快速添加功能正常

---

## 阶段4：用户故事3 - 习惯打卡（优先级：P2）

**目标**: 用户可以创建习惯、每日打卡、追踪连续天数。

**独立测试**: 创建一个习惯，连续打卡5天，验证连续天数计数器显示"5天"。

### 用户故事3的实现

- [x] T024 [P] [US3] 实现 HabitEntity、HabitCheckInEntity、HabitDao、HabitCheckInDao 和 HabitRepository，位于 `data/local/`
- [x] T025 [P] [US3] 实现领域模型（Habit、HabitCheckIn）和仓库接口，位于 `domain/`
- [x] T026 [P] [US3] 创建 GetHabitsUseCase、CreateHabitUseCase、CheckInHabitUseCase、CalculateStreakUseCase，位于 `domain/usecase/habit/`
- [x] T027 [US3] 构建 HabitListScreen，包含习惯卡片、打卡按钮和连续天数显示，位于 `presentation/habits/HabitListScreen.kt`
- [x] T028 [US3] 实现 HabitListViewModel，位于 `presentation/habits/HabitListViewModel.kt`
- [x] T029 [US3] 构建 HabitDetailScreen，包含月度日历热力图和历史记录，位于 `presentation/habits/HabitDetailScreen.kt`
- [x] T030 [US3] 实现习惯打卡的每日通知提醒
- [x] T031 [US3] 添加习惯归档功能（从主列表隐藏但不删除历史记录）

**检查点**: 习惯可以创建、打卡，连续天数计算准确

---

## 阶段5：用户故事4 - 番茄钟（优先级：P3）

**目标**: 用户可以使用番茄钟计时器进行专注会话。

**独立测试**: 开始一个25分钟的专注会话，验证倒计时，让其完成，验证休息计时器启动。

### 用户故事4的实现

- [x] T032 [P] [US4] 实现 PomodoroSessionEntity、PomodoroSessionDao 和 PomodoroRepository，位于 `data/local/`
- [x] T033 [P] [US4] 创建 PomodoroSessionUseCase，包含计时器逻辑，位于 `domain/usecase/pomodoro/`
- [x] T034 [US4] 构建 PomodoroScreen，包含圆形计时器、开始/暂停/重置控制，位于 `presentation/pomodoro/PomodoroScreen.kt`
- [x] T035 [US4] 实现 PomodoroViewModel，包含计时器状态机（专注 → 休息 → 长休息），位于 `presentation/pomodoro/PomodoroViewModel.kt`
- [x] T036 [US4] 实现 ForegroundService，确保应用被最小化时计时器仍能运行
- [x] T037 [US4] 在设置中添加可配置的专注时长、休息时长和长休息间隔
- [x] T038 [US4] 显示会话历史，包含每日专注时间统计

**检查点**: 番茄钟计时器准确运行，跨应用重启持久化，通知正常

---

## 阶段6：用户故事5 - 数据统计与成就（优先级：P3）

**目标**: 用户可以查看生产力统计数据并获取成就徽章。

**独立测试**: 完成几个任务和习惯打卡，验证统计数据正确显示，成就解锁。

### 用户故事5的实现

- [x] T039 [P] [US5] 实现 AchievementEntity、AchievementDao 和 AchievementRepository，位于 `data/local/`
- [x] T040 [P] [US5] 创建 UnlockAchievementUseCase，包含成就条件（完成10个任务、7天连续打卡、50个番茄钟等），位于 `domain/usecase/achievement/`
- [x] T041 [US5] 构建 StatisticsScreen，包含完成率图表、习惯连续天数、任务趋势，位于 `presentation/statistics/StatisticsScreen.kt`
- [x] T042 [US5] 实现 StatisticsViewModel，包含数据聚合逻辑，位于 `presentation/statistics/StatisticsViewModel.kt`
- [x] T043 [US5] 构建成就徽章网格显示，位于 `presentation/statistics/AchievementBadge.kt`
- [x] T044 [US5] 解锁新成就时添加庆祝动画
- [x] T045 [US5] 将成就检查集成到任务完成、习惯打卡和番茄钟完成的流程中

**检查点**: 统计数据准确，成就正确解锁并带有动画

---

## 阶段7：用户故事6 - 主题与个性化（优先级：P3）

**目标**: 用户可以通过多个主题自定义应用外观。

**独立测试**: 切换主题，切换深色模式，验证所有屏幕正确渲染。

### 用户故事6的实现

- [x] T046 [P] [US6] 创建8套配色方案（默认、海洋、日落、森林、熏衣草、樱桃、薄荷、金色），位于 `presentation/theme/Color.kt`
- [x] T047 [P] [US6] 实现 ThemeManager，使用 DataStore 持久化主题偏好，位于 `presentation/settings/SettingsViewModel.kt`
- [x] T048 [US6] 构建主题选择器页面，支持实时预览，位于 `presentation/settings/ThemeSelector.kt`
- [x] T049 [US6] 添加标签颜色自定义（从调色板给标签分配颜色）
- [x] T050 [US6] 在 Android 12+ 设备上支持动态取色（Material You）

**检查点**: 主题切换即时生效，标签颜色可自定义，动态取色支持

---

## 阶段8：打磨与横切关注点

**目的**: 数据备份、错误处理、性能优化和最终打磨。

- [x] T051 实现 JSON 备份/恢复（将所有数据导出到文件，从文件导入并验证），位于 `data/local/backup/BackupManager.kt`
- [ ] T052 添加数据库文件的每日自动备份
- [ ] T053 添加全任务搜索功能（标题+备注），使用全文搜索
- [x] T054 实现滑动删除 + 撤销 Snackbar，位于任务列表
- [x] T055 为任务完成和习惯打卡添加触感反馈和动画
- [ ] T056 支持 Android 快捷方式，用于快速创建任务
- [ ] T057 添加应用小组件，显示今日任务
- [ ] T058 性能优化：LazyColumn 复用、数据库查询优化、内存分析
- [ ] T059 错误处理：优雅的数据库迁移、崩溃恢复、数据完整性检查
- [ ] T060 最终测试：所有用例的单元测试、关键流程的 UI 测试

**检查点**: 应用达到可发布状态，具备备份、搜索、小组件和性能优化

---

## 依赖关系图

```
阶段1（搭建）
  └── 阶段2（US1：待办清单）← MVP
        ├── 阶段3（US2：日历）—— 依赖 US1 的任务数据
        ├── 阶段4（US3：习惯）—— 独立于任务数据
        ├── 阶段5（US4：番茄钟）—— 可独立工作
        ├── 阶段6（US5：统计）—— 依赖 US1、US3、US4 的数据
        └── 阶段7（US6：主题）—— 独立，全局应用
              └── 阶段8（打磨）—— 依赖所有阶段
```

## 可并行执行的任务

| 故事 | 可并行执行 | 说明 |
|------|-----------|------|
| US1（任务） | US3、US4、US6 | 核心功能，最先开始 |
| US2（日历） | US3、US4、US6 | 待 US1 数据就绪后 |
| US3（习惯） | US1、US2、US4、US6 | 独立的数据域 |
| US4（番茄钟） | US1、US2、US3、US6 | 独立功能 |
| US5（统计） | US6 | 依赖 US1、US3、US4 数据 |
| US6（主题） | 全部 | 全局，随处适用 |

## 建议的 MVP 范围

**MVP = 阶段1 + 阶段2（仅用户故事1）**

交付一个可用的待办清单应用，包含任务的增删改查、优先级、截止日期和进度追踪。这提供了即时价值，可以在后续功能开发的同时发布以获得早期反馈。

## 任务总计：60个

| 阶段 | 任务数 | 所属故事 | 优先级 |
|------|--------|----------|--------|
| 阶段1 | T001-T007（7个） | 搭建 | P0 |
| 阶段2 | T008-T018（11个） | US1 | P1 |
| 阶段3 | T019-T023（5个） | US2 | P2 |
| 阶段4 | T024-T031（8个） | US3 | P2 |
| 阶段5 | T032-T038（7个） | US4 | P3 |
| 阶段6 | T039-T045（7个） | US5 | P3 |
| 阶段7 | T046-T050（5个） | US6 | P3 |
| 阶段8 | T051-T060（10个） | 打磨 | P3 |