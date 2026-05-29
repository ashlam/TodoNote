# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**TodoNote** is an Android offline todo & habit management app inspired by the iOS app Todokit. The project is currently in the spec-driven planning phase — the Android codebase has not yet been generated. All development follows the Spec-Kit spec-driven development (SDD) workflow.

Key constraints:
- Completely offline — no network permissions, no cloud sync
- No Google Play Services or Firebase dependencies
- Targets Chinese app store distribution (Huawei, Xiaomi, OPPO, vivo, etc.)
- Kotlin + Jetpack Compose + Room (SQLite) with MVVM + Clean Architecture

## Repository Structure

```
.
├── docs/                          # Design documentation (设计文档.md)
├── memory/
│   └── constitution.md            # Project principles and ADRs
├── specs/001-todonote-app/        # Active feature spec
│   ├── spec.md                    # Functional specification (user stories, requirements)
│   ├── plan.md                    # Technical implementation plan
│   └── tasks.md                   # Task breakdown (60 tasks across 8 phases)
└── spec-kit/                      # Vendored GitHub Spec Kit CLI toolkit (Python)
    ├── src/specify_cli/           # Python source for `specify` CLI
    ├── templates/                 # SDD command templates
    ├── scripts/                   # Bash/PowerShell workflow scripts
    ├── tests/                     # pytest test suite
    └── pyproject.toml             # Python project config
```

## Common Commands

### Spec-Kit CLI Development

The `spec-kit/` directory contains a Python CLI tool. To work on it:

```bash
cd spec-kit

# Install dependencies and CLI in development mode
uv sync --extra test
uv pip install -e .

# Run the CLI
uv run specify --help
uv run specify init <project> --integration copilot

# Run tests
uv run python -m pytest tests/ -v

# Run a specific test file
uv run python -m pytest tests/test_agent_config_consistency.py -q

# Run with coverage
uv run python -m pytest tests/ --cov=src --cov-report=term-missing
```

### Android Project (Future)

Once the Android project is generated per `specs/001-todonote-app/plan.md`:

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.todonote.domain.usecase.task.GetTasksUseCaseTest"

# Build release APK
./gradlew assembleRelease

# Apply code style (Detekt)
./gradlew detekt
```

## Spec-Driven Development Workflow

This project uses Spec-Kit's SDD workflow. The slash commands are defined in `spec-kit/templates/commands/`:

| Command | Purpose | Output |
|---------|---------|--------|
| `/speckit.constitution` | Create/update project principles | `memory/constitution.md` |
| `/speckit.specify` | Create functional spec from description | `specs/<feature>/spec.md` |
| `/speckit.clarify` | Structured clarification of requirements | Updates spec.md |
| `/speckit.plan` | Technical implementation plan | `specs/<feature>/plan.md` |
| `/speckit.tasks` | Generate actionable task list | `specs/<feature>/tasks.md` |
| `/speckit.implement` | Execute tasks and build feature | Code changes |
| `/speckit.analyze` | Cross-artifact consistency check | Analysis report |

**Current feature**: `specs/001-todonote-app/` — spec, plan, and tasks are complete. The next step is `/speckit.implement` to generate the Android codebase.

## High-Level Architecture

### Planned Android Architecture (from plan.md)

The app will use **MVVM + Clean Architecture** with three layers:

```
Presentation Layer (Compose Screens + ViewModels)
         |
Domain Layer (UseCases + Repository Interfaces)
         |
Data Layer (Repository Impl + Room DAO + SQLite)
```

**Package structure** (planned): `com.todonote`
- `data/local/` — Room entities, DAOs, database, type converters
- `data/repository/` — Repository implementations
- `data/local/backup/` — JSON export/import (`BackupManager`)
- `domain/model/` — Pure Kotlin domain models
- `domain/usecase/` — Business logic (testable without Android framework)
- `domain/repository/` — Repository interfaces
- `presentation/` — Compose UI, ViewModels, navigation, theme
- `di/` — Koin modules (`AppModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule`)

### Key Technical Decisions (from constitution.md)

- **Koin over Hilt/Dagger** — Pure Kotlin, no annotation processing, no Google dependencies
- **Room over raw SQLite** — Type-safe, compile-time verified, part of Jetpack (not Play Services)
- **Navigation Compose** — Single-activity architecture, no deep links
- **AlarmManager + NotificationManager** — Local reminders instead of FCM
- **ForegroundService** — Pomodoro timer persists when app is backgrounded
- **DataStore Preferences** — Settings storage instead of SharedPreferences

### Database Schema (8 tables)

Core: `ListEntity`, `TaskEntity`, `TagEntity`, `TaskTagCrossRef`
Habits: `HabitEntity`, `HabitCheckInEntity`
Pomodoro: `PomodoroSessionEntity`
Achievements: `AchievementEntity`

## Development Standards

From `memory/constitution.md`:

- **Privacy by Design**: Zero network requests. No `INTERNET` permission in `AndroidManifest.xml`.
- **Testability**: Domain layer 100% unit test coverage. UseCases must be pure Kotlin with no Android framework dependencies.
- **Performance**: Cold start < 3s on mid-range devices. Lists use `LazyColumn` with stable keys. Room queries indexed.
- **Code Style**: Kotlin official conventions, Detekt static analysis, max 60 lines/method, max 400 lines/class, explicit public return types.
- **Git**: Conventional Commits (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `test:`), one feature per branch, squash merge to `main`.
