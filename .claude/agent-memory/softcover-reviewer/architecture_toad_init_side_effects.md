---
name: architecture_toad_init_side_effects
description: A ScreenModel's init block should trigger one-shot side effects via a Collector.onLaunch or by dispatching an Action, not by calling dependencies.launch{} directly
metadata:
  type: project
---

The foundation toad-architecture doc (docs/rhaydus/0.3.1/toad-architecture.md) documents exactly two
channels for state-affecting work: a `Collector.onLaunch` ("runs once when the screen model is
created... via startInitializers()") for repo-flow observation and one-shot init work, and a
`UiAction.execute()` ("one action = one user intent or **triggered effect**") for anything else. Every
`init {}` in this codebase's `*ScreenModel`s calls only `startInitializers()`, except
`BookDetailScreenScreenModel`, which fires its one-time load via `dispatch(InitializeBookWithIdAction(id
= bookId))` — i.e. still through the Action pipeline.

Worked example (found and fixed in the "Hidden suggestions" review, 2026-07-13):
`HiddenSuggestionsScreenModel.init` originally called
`dependencies.launch { dependencies.enrichDismissedContinueSeriesMetadataUseCase() }` directly from
`init`, bypassing both blessed channels. `ActionDependencies.launch` is meant to be called *from
within* an Action/Collector (e.g. the "undo" snackbar's deferred re-hide callback in
`OnUnblockBookAction`), not from the ScreenModel itself. The fix was a dedicated
`EnrichMetadataCollector : HiddenSuggestionsCollector` whose `onLaunch` runs the use case once — so
`init` again only calls `startInitializers()`. Prefer that shape (or a dispatched initialize Action).

**Why it matters:** it's the one place in the codebase where a ScreenModel does something outside
`startInitializers()` + `dispatch()`, which breaks the "screen model's dispatch/collector pipeline is
the only way state-affecting work happens" invariant relied on elsewhere (testability via Action
dispatch, consistency of the data-flow diagram in the architecture doc).

**How to apply:** flag a raw `dependencies.launch { dependencies.<useCase>() }` inside a ScreenModel's
`init` as a 🟡 architecture finding. Suggest either a dedicated Collector whose `onLaunch` runs the
use case once, or dispatching an initialize Action (mirroring `BookDetailScreenScreenModel`).
