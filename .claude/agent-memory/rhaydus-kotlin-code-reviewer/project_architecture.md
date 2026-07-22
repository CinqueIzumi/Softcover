---
name: project_architecture
description: Softcover Android app structure — Clean Architecture + TOAD state framework on Voyager, Apollo GraphQL, Room, Koin, Jetpack Compose
metadata:
  type: project
---

Softcover is a native Android Hardcover.app client. Kotlin + Jetpack Compose, targeting SDK 26+.

Core stack: Apollo GraphQL (queries in `app/src/main/graphql/`), Room database with manual migrations in `SoftcoverDatabase.kt`, Koin DI, Voyager navigation, custom TOAD state framework.

TOAD: each screen has UiState (StateFlow), UiAction (sealed, execute()), UiEvent (Channel), LocalVariables, ActionDependencies, Initializers (Flow collectors in `flows/`).

Layer structure per feature: domain/ → data/ → presentation/ → di/. Presentation depends on domain only.

**Why:** Reference architecture for all feature additions and layer-placement decisions.
**How to apply:** Check docs/reference/architecture.md before adding new features or reviewing DI/navigation code.
