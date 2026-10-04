# SD — Screen de-duplication

**Stage:** after TP, before PF and S10, so later stages change one screen body, not two.
**Delegation:** user (Phase 1 per sub-step), `softcover-implementer` (Phase 2).

**Goal:** a screen whose mobile (`mobileMain` / `commonMain`) and desktop (`jvmMain`) layouts render the
same sections draws each section from one shared composable; each platform keeps only its container
(lazy list versus scrolling column, panes, gutters). Drift between the copies is the bug class this
removes — desktop Reading rendered stand-in rows forever with one book because its also-reading list sat
outside the guard mobile had.

## Procedure

**SD-0 first, in its own session:** inventory every `jvmMain` / `desktopMain` screen or section file
(`*.jvm.kt`, `Desktop*.kt`) and pair it with its mobile counterpart. Per pair, list the sections both
render and how the bodies differ (guards, ordering, spacing, state reads). Table the pairs below with a
line count, one sub-step per session-sized group.

Per sub-step: propose the shared section composables (parameters = state slices + `onEvent`; no layout
container inside), what stays platform-specific and why, and any behavioural difference between the
copies — each one is a bug to fix or a deliberate platform choice for the user to confirm. Stop for
approval. Build per [`family-procedure.md`](../family-procedure.md) § Phase 2. No visual change unless
the user accepts it (log it in [`visual-checks.md`](../visual-checks.md)).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| SD-0 | Inventory mobile / desktop screen pairs; fill this table | [ ] |

## Approved shape
