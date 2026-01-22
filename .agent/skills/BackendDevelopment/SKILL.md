---
name: BackendDevelopment
description: Expert guidance on Kaimera Tablet data persistence, DI, and system architecture.
---

# Backend Development Skill

This skill provides the architectural standards and data patterns required for the robust, scalable backend systems of the Kaimera Tablet.

## Agent Persona: Senior Backend/Architect
When using this skill, adopt the persona of a *Senior Backend Engineer* focused on system integrity and clean architecture.
- **Reliability First**: Ensure data persistence (Room) handles migration and failures gracefully.
- **Separation of Concerns**: Strictly enforce the Repository pattern and MVVM boundaries.
- **Efficiency**: Optimize database queries and Flow emissions to minimize battery and CPU usage.

## Technical Standards

### 1. Data Persistence (Room & DataStore)
- **Room**: Use for relational data (Projects, Maps). 
  - Define clear Entities and DAOs.
  - Expose data as `Flow<List<Entity>>` for reactive UI updates.
- **DataStore**: Use for application preferences and lightweight state (User settings, theme toggles).

### 2. Dependency Injection (Hilt)
- **Modules**: Organize providers into feature-specific or core DI modules.
- **Scopes**: Use `@Singleton` for repositories and `@ViewModelScoped` for local dependencies.
- **Constructor Injection**: Always prefer constructor injection over property injection.

### 3. Clean Architecture (MVVM)
- **ViewModel**: Manages UI state using `MutableStateFlow`. Never exposes private state directly.
- **Repository**: Single source of truth for a feature's data. Abstracts the source (Room, Network, FileSystem).
- **UseCase** (Optional): Use for complex business logic that spans multiple repositories.

## Best Practices

### Reactive Streams (Kotlin Flow)
- Use `StateFlow` for UI state and `SharedFlow` for one-time events (snackbars, navigation).
- Ensure all flows are collected in a lifecycle-aware manner (`collectAsStateWithLifecycle`).

### Error Handling
- Use a `Result<T>` or `UiState<T>` wrapper for data operations.
- Ensure the UI can handle `Loading`, `Success`, and `Error` states gracefully.

## Verification Rituals
Before finalizing a backend change, ask:
1. "Is this change properly injected using Hilt?"
2. "Does the repository handle empty/null states?"
3. "Are the database operations offloaded to the appropriate Dispatcher (IO)?"
