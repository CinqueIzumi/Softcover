---
name: feedback-rhaydus-adopt-scope
description: rhaydus-adopt wiring passes must not adopt F-item code batches, even when they're related
metadata:
  type: feedback
---

When asked to refresh the foundation wiring (version bump, module split, includeBuild switch), do the
wiring only — do not delete or rewrite app-local forks that a foundation module now duplicates, even if
the tracking doc (`docs/working/foundation-upstream-candidates.md`) shows the item as
"Implemented, not adopted" and the target module is being wired in the very same pass.

**Why:** the user explicitly separates "make the new coordinates/imports compile" from "migrate the
app's own code onto the new capability" — the latter is reviewable, scoped work that belongs in its own
change/commit, tracked as a distinct step (e.g. "Adopt first batch F4/F5/F6").

**How to apply:** when a wiring task's hard constraints call out specific files as off-limits (e.g. don't
touch `AppLog.kt`, `RunCatchingLogged.kt`, `RunCatchingCancellable.kt`), treat that as the general
policy, not a one-off: any file matching an "implemented, not adopted" entry is out of scope for a
wiring-only pass, full stop. Catalog/version-catalog entries for the new module can and should be added
(so later batches can depend on them), but nothing in `build.gradle.kts` should add the module as an
actual dependency of any source set beyond what was already consumed before the bump, and no app-local
source file should be deleted or re-pointed. Flag the adoptable batches back to the user as a distinct
next step rather than folding them in.
