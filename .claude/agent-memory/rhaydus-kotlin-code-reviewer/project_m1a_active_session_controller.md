---
name: project_m1a_active_session_controller
description: M1a refactor context — ActiveSessionController moved from class in core:designsystem to interface+impl split; impl in orchestration/session/
metadata:
  type: project
---

`ActiveSessionController` is now an interface in `:core:designsystem` (same FQN, so all consumers untouched). Impl is `internal class ActiveSessionControllerImpl` in `:orchestration/session/`. Koin binding moved from `designSystemModule` to `orchestrationModule` as `single<ActiveSessionController> { ... }`. `:core:personal` dep removed from `:core:designsystem`. Pattern mirrors `AppNavigator`. All consumers inject by type (`koinInject<ActiveSessionController>()` or `by inject()`). Test moved to `ActiveSessionControllerImplTest` in orchestration androidHostTest.

**Why:** `:core:designsystem` was a god-module pulling `:core:personal` use cases. The seam moves that responsibility to `:orchestration`, which already owns `:core:personal`.

**How to apply:** Any future session-control expansion (new method on the interface) must be added to the interface in `:core:designsystem` AND implemented in `ActiveSessionControllerImpl` in `:orchestration`. The interface is the cross-tier contract.
