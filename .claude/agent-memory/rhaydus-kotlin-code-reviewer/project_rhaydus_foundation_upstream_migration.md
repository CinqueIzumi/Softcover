---
name: project_rhaydus_foundation_upstream_migration
description: rhaydus-foundation is a sibling stand-alone repo Softcover depends on; app-local code is being migrated upstream into it in batches
metadata:
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

**Batch H (`offline-sync` module, release/0.3.0, F8) review notes (2026-07-03):** a generic
optimistic-write drain engine extracted from two Softcover syncers. Clean extraction (grepped for
softcover/Room/Apollo/Book/etc - none found outside one KDoc example sentence), sensible `<P, I, K>`
generic split (`WriteQueue<P>`/`PendingWriteStore<P>` vs. `OfflineWriteDrainer<I, K>` so plain
enqueue-only call sites don't need the reconciliation type params), `isTransient` defaulting to
`{ true }` (never silently discard) is the right data-safe default and is well documented. `DrainPolicy`,
`ReplayOutcome`, one-declaration-per-file, KDoc-on-public-surface, blank-line/paragraph rules, import
order - all compliant on read-through.
Found one **confirmed** (empirically reproduced, see [[feedback_onstart_stateflow_double_emit]]) bug:
`start()`'s `.onStart { if (isOnline.value) drainGuarded() }.onEach { if (it) drainGuarded() }.collect()`
double-drains on startup when already online, because `isOnline` is a `StateFlow` and replays its
current value to the `onEach` regardless of the redundant `onStart` guard.
Also found a real observability gap worth calling out in any similar drain/retry engine review: the
per-row failure paths (`store.getPending()` throwing → swallowed via `.getOrElse { emptyList() }` with
no log; a transient `replay()` failure → caught, `incrementAttempts`'d, and silently returned from the
loop with no log) never reach the one log call the class has (`drainGuarded`'s outer
`runCatchingLogged`), because `drainOnce()` never actually throws for those categories - only an
app-supplied `hintKey()` throwing (or a `store.delete`/`incrementAttempts` bug) would surface there. A
write that permanently crosses the poison cap (`DrainPolicy.maxAttempts`) is never logged as "stuck" -
it just silently stops being returned by `getPending` forever. Worth flagging as a design gap in any
review of this file: the class's own KDoc claims background drains are "resilient... a failure is
logged," but that log path is barely reachable in practice.
Also: `hintKey()` is called unguarded inside the `onSuccess` handler, *after* `store.delete(row.localId)`
already ran - if it throws, that row's hint is lost forever (row already gone) and the exception aborts
processing of the rest of that drain pass's remaining rows too, asymmetric with how `replay`/`isTransient`
failures are contained. Same class of "seam callback isn't defensively wrapped" issue as the log gap
above.
Naming: concrete class is `DefaultOfflineWriteDrainer`, not `*Impl` - checked, no existing
`Default*`/`*Impl` precedent either way elsewhere in the foundation modules (core-platform uses
per-platform names like `AndroidNetworkAvailabilityProvider`/`BaseNetworkAvailabilityProvider`, not
`Impl`). Code-style's `*Impl` convention is framed around app-feature data-layer types
(`*RepositoryImpl`/`*DataSourceImpl`/`*StorageImpl`/`*QueueImpl`); a foundation-shipped canonical
"default" engine implementation that apps might swap out is a different shape, and `Default*` is a
defensible, arguably more idiomatic library-naming choice here - not a violation, just worth noting if
asked.
Doc: new `docs/architecture.md` §6 subsection is accurate, correctly placed, and cross-referenced with
CAPABILITIES.md's module row + dependency-graph line in the same change (maintenance rule satisfied).
One newly-added em dash in the new architecture.md prose sentence - technically violates
`CLAUDE.md`'s "docs are em-dash-free" rule for markdown docs (this rule does NOT extend to `.kt` files,
see the em-dash correction note above, but DOES apply to markdown prose); minor, and one pre-existing
em dash was already in the file untouched by this diff, so the codebase wasn't at zero to begin with.
Test gap: the "in-drain backoff" test only asserts `callCount == 3`, not the actual delay/backoff
progression (no assertion on `testScheduler.currentTime` or equivalent) - the exponential-backoff math
itself (`initialBackoffMs` × `backoffMultiplier`, capped at `backoffCapMs`) is undocumented-by-test even
though it's a documented, tunable behavior.

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

**Import-order rule scope (verified 2026-07-03, Batch G `ShareCardCapture`):**
`ktlint-rules/src/main/kotlin/nl/rhaydus/ktlint/ProjectImportOrderRule.kt` only gates alphabetical order
within the `nl.rhaydus.*` import block. It explicitly does NOT check ordering inside the Android/AndroidX
group or the third-party group (its own KDoc says so: "the Android / third-party groups are left to the
IDE"). Don't flag a file where `java.awt.*`/`javax.*` imports land after `kotlinx.coroutines.*`/
`org.jetbrains.*` (not strictly alphabetical) as a style violation - that's normal and ungated as long as
there's no `nl.rhaydus.*` block out of order.

**`Dispatchers.IO` on Kotlin/Native is public in kotlinx-coroutines 1.10.2 - a "PR says it's internal"
claim needs verification, not trust (Batch G):** the Batch G iOS `ShareCardCapture` actual used
`Dispatchers.Default` for file I/O with the stated rationale "`Dispatchers.IO` is internal on K/N in
this coroutines version." That's wrong. Verified by unzipping the pinned
`kotlinx-coroutines-core-iosarm64-1.10.2-sources.jar`: `concurrentMain/Dispatchers.kt` declares
`public expect val Dispatchers.IO`, and `nativeMain/Dispatchers.kt` provides
`public actual val Dispatchers.IO: CoroutineDispatcher get() = IO` (the only `internal` thing is an
unrelated same-named member on the `Dispatchers` object itself, shadowed for external callers). Softcover's
own `core/domain/.../DispatcherModule.kt` (commonMain, compiled for iOS too) already calls
`Dispatchers.IO` successfully via an explicit `import kotlinx.coroutines.IO`. Likely root cause of the
wrong belief: on Native, `Dispatchers.IO` is an *extension* property (not a direct member like on JVM), so
referencing it without that explicit import is an unresolved-reference compile error that reads like "IO
doesn't exist here" - easy to misdiagnose as "internal." **When a diff's rationale claims a stdlib/coroutines
API is "internal"/"unavailable" on a KMP target, verify by grepping the pinned artifact's sources jar
before accepting the claim** - don't just take the code comment at face value.
