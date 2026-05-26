# TodoNote Project Constitution

**Created**: 2026-05-26

**Project**: TodoNote — Android offline todo & habit management app

---

## Core Principles

### 1. Privacy by Design
- Zero network requests. No data ever leaves the device
- No analytics SDK, no crash reporting tools that send data externally
- No NETWORK permission in AndroidManifest.xml
- All data belongs to the user — provide easy export/import

### 2. Local-First Architecture
- Every feature must work 100% offline
- No feature should have a "requires internet" state
- Background sync is out of scope — all persistence is local SQLite
- Use Room database with reactive Flows for real-time UI updates

### 3. No Google Lock-In
- Zero dependency on Google Play Services
- No Firebase suite (FCM, Firestore, Auth, Analytics, Crashlytics)
- Notifications via Android's NotificationManager + AlarmManager
- Distribution via Chinese app stores (华为, 小米, OPPO, vivo, 应用宝)
- In-app updates via APK download (not Play In-app Updates)

### 4. Material Design 3 with Chinese UX Sensibilities
- Follow Material 3 design system but adapt to Chinese user expectations
- Support simplified Chinese as first-class language
- Use culturally appropriate icons, colors, and layouts
- Dark mode auto-follow system setting
- Support for common Chinese date formats (2024年12月25日)

### 5. Testability & Code Quality
- Business logic in UseCases must be pure Kotlin — testable without Android framework
- Repository interfaces in domain layer allow test doubles
- ViewModels use constructor injection for testability
- Unit test coverage > 80% for domain and data layers
- Compose UI tests for critical user flows

### 6. Performance Awareness
- All lists must use LazyColumn/LazyRow (no ScrollView for dynamic content)
- Room queries must be indexed for common access patterns
- Compose recomposition must be minimized (stable keys, derivedStateOf)
- Cold start under 3 seconds on mid-range devices (Snapdragon 7-series, 6GB RAM)
- APK size under 20MB

### 7. User Experience
- Every action must have haptic or visual feedback
- Empty states must be helpful, not blank (illustration + suggestion)
- Swipe gestures: swipe to delete with undo
- Drag-and-drop for reordering lists and tasks
- Progress indicators for lists (e.g., "3/8 completed")

## Development Standards

### Code Style
- Follow Kotlin coding conventions (official)
- Use `Detekt` for static analysis
- Maximum method length: 60 lines
- Maximum class length: 400 lines
- Use explicit return types for public functions

### Git Convention
- Branch: `feature/<short-name>` or `fix/<short-name>`
- Commits: Conventional Commits (feat:, fix:, chore:, refactor:, docs:, test:)
- One feature per branch, squash merge to main

### Testing Requirements
- Domain layer: 100% unit test coverage
- Data layer: 90% unit test coverage (DAOs via Room in-memory database)
- Presentation layer: Critical user flows covered by Compose UI tests
- Smoke test before every release: create task → complete → verify

## Architecture Decision Records

### ADR-001: Koin over Hilt/Dagger
**Context**: Need DI framework that doesn't require annotation processing with Google dependencies.  
**Decision**: Use Koin — pure Kotlin, no code generation, no Google-specific dependencies.

### ADR-002: Room over raw SQLite
**Context**: Need type-safe database with compile-time query verification.  
**Decision**: Room is part of Android Jetpack (not Play Services), works completely offline. Accept the annotation processing dependency (kapt/ksp) as it's a build-time only concern.

### ADR-003: No Navigation architecture component for deep linking
**Context**: Single-user offline app doesn't need deep linking.  
**Decision**: Use Navigation Compose for screen routing but don't configure deep links.