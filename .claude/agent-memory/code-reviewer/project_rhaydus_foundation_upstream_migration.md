---
name: project_rhaydus_foundation_upstream_migration
description: rhaydus-foundation is a sibling stand-alone repo Softcover depends on; app-local code is being migrated upstream into it in batches
type: project
---

`rhaydus-foundation` is a separate git repo checked out as a sibling of this one (relative path
`../rhaydus-foundation`; never record an absolute path - it is machine-specific). It has its own
CLAUDE.md and `docs/code-style.md` / `docs/architecture.md` / `docs/CAPABILITIES.md`, and is NOT part of
the Softcover working tree. Softcover consumes it as `nl.rhaydus:*` Maven artifacts and vendors pinned
copies of its docs under `docs/rhaydus/<version>/`.

Softcover keeps a "foundation-upstream-candidates queue" - working app-local code gets generalized and
ported up into the foundation's `core-*` modules in labelled batches (e.g. Batch B = F9 `SecureStorage`
+ F10 `NetworkAvailabilityProvider`/`NetworkAvailability`, landed in the new `nl.rhaydus:core-platform`
module, ported from Softcover's `SecureApiKeyStorage` / `NetworkAvailabilityProvider` /
`ConnectivityDataSource` / `ConnectivityRepositoryImpl`).

**Why**: keeps app-local, battle-tested code from calcifying as app-only; grows the foundation's
reusable non-visual seam surface (mirrors the existing `AppDispatchers`/`AppLog` precedent: types-only,
no Koin module, app wires DI on adopt).

**How to apply**: when reviewing a rhaydus-foundation change, read that repo's OWN `docs/code-style.md`
and `CLAUDE.md` (not Softcover's) - they are the source of truth there, and drift between the two repos'
docs is expected during migration (the foundation's rules are sometimes stricter or newer).

**Correction (verified 2026-07-03): the "docs are em-dash-free" rule in `CLAUDE.md` does NOT reach
Kotlin comments/KDoc** - do not flag em dashes in `.kt` files as a violation. Evidence: `CLAUDE.md`
itself uses em dashes in its own prose (e.g. its Versioning section), and a repo-wide grep found em
dashes already in ~8/80 `.kt` files across `designsystem-core`/`core-common` (`AppLog.kt`,
`RunCatchingCancellable.kt`, `RunCatchingLogged.kt`, several test display names). The rule is scoped to
markdown docs/prose, not source comments. (A prior version of this memory claimed the opposite and was
wrong - corrected after reviewing F13/F14 `StarRatingInput`/`ExpandableFlowRow`.)

Also useful: `docs/CAPABILITIES.md` (the component/module index) and the doc that governs the changed
area (e.g. `docs/design-system-foundations.md` for a design-system component) both have a
review-enforced maintenance rule requiring them to be updated in the same change that adds/changes a
component - check both are current, don't just check one.

**Doc-drift trap found in review (2026-07-03, F/desktop-scrollbar + platformModifierClick batch):**
`design-system-foundations.md` §11's intro sentence said "the **two** modifiers below are in
`commonMain` and no-op on touch" (accurate count for `pointerHandCursor` + `hoverHighlight` at the
time it was written). The new change added a third commonMain/no-op modifier
(`platformModifierClick`) into the same §11 bullet list without updating that lead-in count/wording -
so the doc edit satisfied the "add a bullet for the new component" rule but broke the accuracy of a
sentence it didn't touch. **Lesson: when a change inserts a new bullet into an existing enumerated
list, grep the surrounding prose for a stated count ("two X below", "both Y", "the three Z") and
verify it still holds** - this class of drift is easy to miss because the diff never touches the
sentence that becomes wrong.

Also confirmed in this batch: the `<Name>.jvm.kt` / `<Name>.mobile.kt` file-suffix convention for
expect/actual is well-established precedent (`DesktopTooltip`, `DesktopContextMenu`, `Haptics`,
`ClipboardReader`, `ReducedMotion`, `NumberFormat`), and `mobileMain` is a real wired-up intermediate
source set (`KmpLibraryConventionPlugin.kt`: `applyDefaultHierarchyTemplate()` + manual
`androidMain`/`iosMain` `dependsOn(mobileMain)`) - not a typo/dead source set, so a new
`src/mobileMain/...` file for an `expect` in `commonMain` is correctly wired without per-module
Gradle changes.

**Test-coverage heuristic confirmed (Batch F, `InlineErrorState`, 2026-07-03):** whether a new
`designsystem-core` component has a companion test file correlates with whether it carries internal
state/logic, not with its visibility or how "important" it looks. Pure-render wrapper composables with
no branching/state (`RhaydusButton`, `InlineErrorState`, `DesktopVerticalScrollbar`) correctly have no
test file; components with real internal logic (`StarRatingInput`'s drag-scrub math, `BottomBarScaffold`'s
measured footprint, `WindowSizeClass`'s breakpoint math, `NavPulse`'s signal semantics) do. Don't flag a
missing test file as a gap just because the component is new and public - check whether it has any
testable logic first.

**Batch F / InlineErrorState review notes:** the component is a straight lift-and-generalize (hardcoded
`"Retry"` + app's `editorialTypography.bodySmall` → `retryLabel`/`textStyle` params) and came out clean
on the full code-style pass (import order, arg-layout, blank-line-between-siblings, visibility, KDoc).
Only two 🔵 nits surfaced: (1) a named-arg call to a shared component (`RhaydusButton(label=, onClick=,
style=, size=, modifier=)`) reordered relative to both the declaration and the "modifier right after
required params" convention - harmless since named, but worth a pass-over on any call site with 4+ named
args; (2) a doc-illustration field-name drift in `toad-architecture.md`'s new "error-slot + retry
convention" bullet - it introduces the example field as `searchError` then folds via
`it.copy(error = message)` two lines later. Same class of issue as the §11 count-drift above: an
inline code example one line away from its own definition still needs a consistency check, not just the
enumerated-list-count check.
