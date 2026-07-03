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
