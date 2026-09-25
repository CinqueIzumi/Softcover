---
paths:
  - "feature/**"
  - "core/**"
  - "orchestration/**"
---

# Architecture quick reference

Detail: `docs/rhaydus/0.3.1/architecture.md` and `toad-architecture.md`, then `docs/reference/architecture.md`
(Apollo, Room, module overview, TOAD notes) and `docs/reference/module-structure.md` (roster, tiers).

- **Modules:** `:app` (shell) → `:orchestration` (nav host + cross-feature use cases) → `:feature:*` → `:core:*`.
  **A feature never imports a sibling feature.**
- **Layers per feature:** `domain/` (repository interfaces + use cases, depends on nothing) → `data/` (impls,
  data sources, mappers; Room entities/DAOs live in `:core:database`) → `presentation/` (screens, ScreenModels,
  actions, events, state; depends on domain only) → `di/` (Koin module).
- **TOAD** (on Voyager's `ScreenModel`): immutable `UiState` as a `StateFlow`; a sealed `UiAction`, one per
  interaction; one-time `UiEvent` via `Channel`; `LocalVariables`; `ActionDependencies`; per-feature
  `*Collector` interfaces in `flows/`. Flow: `UiAction.execute()` → use cases → `setState()` → recompose.
- **Always:** Apollo via `safeQuery()` / `safeMutation()` (queries in `core/network/src/commonMain/graphql/`);
  Room + migrations in `:core:database`; DataStore for preferences; Koin DI; Voyager navigation;
  `AppDispatchers` for Main/IO/Default; `Result<T>` with `.onSuccess()` / `.onFailure()`; `AppLog` for logging,
  never `println` / `Log.*`.
- **Naming:** domain models are plain nouns (`Book`, `Author`). Suffixes mark the role: `*Entity`,
  `*DataSource(Impl)`, `*Repository(Impl)`, `*UseCase`, `*Screen`, `*ScreenModel`, `*Action`, `*Event`,
  `*UiState`, `*LocalVariables`, `*Dependencies`, `*Collector`.
