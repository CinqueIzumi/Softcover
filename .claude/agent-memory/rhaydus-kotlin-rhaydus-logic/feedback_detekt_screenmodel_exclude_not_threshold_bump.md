---
name: detekt-screenmodel-exclude-not-threshold-bump
description: When a TOAD ScreenModel/Dependencies constructor trips detekt's LongParameterList, exclude the presentation/screenmodel path instead of bumping the numeric threshold again
metadata:
  type: feedback
---

When adding a new use case to a TOAD `*ScreenModel` / `*Dependencies` constructor pushes it over
detekt's `LongParameterList.constructorThreshold` in `config/detekt/detekt.yml`, do NOT just bump the
number again. Instead add/extend a path-based `excludes` glob for the rule:

```yaml
LongParameterList:
  excludes: ['**/presentation/screenmodel/**']
  functionThreshold: 12
  ignoreDefaultParameters: true
```

**Why:** Bumping `constructorThreshold` (e.g. 32 → 33 → 34, one bump per feature added) was flagged by
the user during roadmap step 10.16 Phase 2 (2026-07-22) as an obviously unsustainable pattern — the
count has no natural ceiling since Koin constructor DI keeps growing with every new use case, and a
numeric threshold loosens the check for *every* class in the whole app, not just the DI-heavy TOAD
layer it was meant for. detekt's rule-level `excludes` (glob, matched against file path) already had
precedent in this exact file (`coroutines.InjectDispatcher` excludes `**/di/**`) and in the foundation's
own bundled baseline comment ("TOAD *ScreenModel / *Dependencies inject many use cases via Koin; a
parameter object is not idiomatic for constructor DI"). Every feature module keeps its ScreenModel +
Dependencies under `presentation/screenmodel/` by convention (verified across book_detail, explore,
library, lists, onboarding, profile, reading, scan, settings), so excluding that one path targets
exactly the classes the rationale is about and leaves the rule at full strength everywhere else.

**How to apply:** If a future task hits this same violation again, do not touch the numeric threshold
at all — confirm the violating file lives under `presentation/screenmodel/` (it will, by convention)
and treat the existing exclude as already covering it. If a *different* long-parameter-list violation
shows up outside that path, that's a real finding — don't reach for excludes/threshold bumps to make it
go away.
