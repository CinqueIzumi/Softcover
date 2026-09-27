# Component Library Migration — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes S12
> ([`steps/s12-close-out.md`](steps/s12-close-out.md)). The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Rollout model:** one branch, one PR, merged all at once. Stages are commit boundaries on that
branch, not separate pull requests. Every stage boundary must leave the branch compiling.

**Branch:** `275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`
**Issue:** [#275](https://github.com/CinqueIzumi/Softcover/issues/275) — tag `E.1`, labels
`area:cross-cutting` / `kind:tech` / `scope:L`, no milestone. Keep its Stages and Acceptance
checkboxes in step with `## Steps` and `## Gates` here.

## Now

- **State:** S0–S4 and S5-1 through S5-3b done. S5-C-chips (the chip convergence pass; its step file is
  already deleted) is mid-flight: one `ChipScaffold` + `ChipStyle`, shared `ChipDimensions` + `ChipSize`,
  and the tone / slot model (`ChipTone` + `selected`, `ChipLeading` / `ChipTrailing`, `face`) are built;
  every host test passes against them.
- **Approved, not built:** features hold chips as a generic `ChipSet<P>(chips, payloadByKey)` from
  `:core:component`, replacing every chip list + `…ByChipKey` map pair (explore, library, book_detail).
- **Also built:** plan-reference gates (detekt `ForbiddenComment`, `doc-guard.sh`, `checkDocBudgets`).
- **Next:** `softcover-test-writer` adds cases for the gate fix round: the step-id pattern in
  `check_plan_refs` / `CheckDocBudgetsTask`, and trailing comments in `check_kotlin_comments`; then a
  short `softcover-reviewer` re-check.
- **After that:** (1) re-run the agent-memory audit read-only, then apply it (delete one-offs, promote
  durable rules to docs); (2) memory gates: `/handoff` audits memory when a `steps/*.md` was deleted,
  memory budgets in `checkDocBudgets`, `.claude/agent-memory/**` in the plan-reference scan; (3)
  `ChipSet<P>`, tests, reviewer over S5-C-chips; (4) [`steps/s5-headers.md`](steps/s5-headers.md) S5-4.
- **Due when:** Step 09 in `docs/working/token-hygiene/README.md` — every S5 and S6 sub-step is ticked,
  convergence passes included (`steps/s5-*.md`, `steps/s6-*.md`)
- **Open questions:** none. Check on device whether the Arrange / Filter sheet chip rows render empty or
  stale for a frame on open; if visible, seed them in the open action.
- **Verification:** chip host tests pass; `checkDocBudgets styleCheck`, `-p build-logic test` and
  `run-doc-guard-cases.sh` pass after the gate fix round.
- **Uncommitted:** chip package, call sites and host tests; plan-citation KDoc cleanup; the gates and
  their tests; `.claude/rules/docs.md`; reference docs; this tracker and step files; new agent memories.

## Local verification

Two pre-existing toolchain problems on this machine:

1. The aggregate `check` lifecycle cannot complete — `:app:compileDebugJavaWithJavac` fails inside
   `JdkImageTransform` (`jlink` from Homebrew JDK 26.0.2 against `android-37.0`).
2. detekt cannot run on JDK 26 at all — prefix every Gradle call with JDK 21:
   `JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home`.

Run Gradle through `scripts/gradle-quiet.sh`, not `./gradlew` directly. The plain `test` lifecycle task
runs no KMP suites (`:app` / `:desktopApp` only); use `testAndroidHostTest --continue` under JDK 21 for
the KMP suites.

**Reliable per-change gates:** `checkModuleGraph`, `<module>:projectHealth`,
`<module>:compileKotlinJvm`, `ktlintCheck`, `<module>:detektJvmMain`, and **`styleCheck`** — all under
JDK 21. `styleCheck` is the only gate that catches detekt `MatchingDeclarationName`; include it in
every sub-step's Verify.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Read this file's `## Now` block, [`family-procedure.md`](family-procedure.md), and the one step
   file `## Now` names. Do not read the other step files.
3. Every sub-step ends the same way: acceptance passes → update `## Now` → tick the sub-step in the
   step file's `## Sub-steps` table and the matching row below → tick the box on
   [#275](https://github.com/CinqueIzumi/Softcover/issues/275) via `gh` → delete the step file once its
   last sub-step lands, and drop its `## Steps` link → **ask the user before committing** (subject-only
   message) → `/handoff` → the user types `/clear`.

## Steps

| Stage | Step file | Sub-steps | Status |
|---|---|---|---|
| S0–S4 | — | module scaffolding through tokens-only designsystem | [x] |
| S5 | — (chips) | S5-1–S5-3b, S5-C-chips | [x] |
| S5 | [steps/s5-headers.md](steps/s5-headers.md) | S5-4–S5-6 | [ ] |
| S5 | [steps/s5-badges.md](steps/s5-badges.md) | S5-7 | [ ] |
| S5 | [steps/s5-skeletons.md](steps/s5-skeletons.md) | S5-8 | [ ] |
| S5 | [steps/s5-dividers.md](steps/s5-dividers.md) | S5-9 | [ ] |
| S6 | [steps/s6-list-rows.md](steps/s6-list-rows.md) | S6-1–S6-2 | [ ] |
| S6 | [steps/s6-sheet-chrome.md](steps/s6-sheet-chrome.md) | S6-3–S6-7 | [ ] |
| S7 | [steps/s7-bookcard.md](steps/s7-bookcard.md) | S7-1–S7-7 | [ ] |
| S8 | [steps/s8-screen-states.md](steps/s8-screen-states.md) | S8-1–S8-3 | [ ] |
| S8 | [steps/s8-controls.md](steps/s8-controls.md) | S8-4–S8-5 | [ ] |
| S9 | [steps/s9-statistics.md](steps/s9-statistics.md) | S9-1–S9-4 | [ ] |
| S10 | [steps/s10-shelf-teardown.md](steps/s10-shelf-teardown.md) | one per `*Shelf.kt` | [ ] |
| S11 | [steps/s11-contract-retrofit.md](steps/s11-contract-retrofit.md) | S11-1–S11-3 | [ ] |
| S12 | [steps/s12-close-out.md](steps/s12-close-out.md) | S12-1–S12-4 | [ ] |

## Decisions

- Test posture: no Compose UI tests; unit tests cover every UI model and mapper, and the Component Gallery is the visual-acceptance surface.
- A `:core:uibinding` → `:core:<data>` edge is pre-approved when a mapper needs one; write it when the edge exists.
- A step spanning several sessions splits into sub-steps, one session each, in the step file's `## Sub-steps`.

## Baseline

Measured 2026-08-21, `release/3.2.0` @ `ada51050`.

| Metric | Value |
|---|---|
| `@Composable` declarations, whole repo | ~650 |
| …in `core:designsystem` at baseline | 132 |
| …feature-local | ~500 |
| Lines in composable-bearing `commonMain` files | 29,159 |
| …concentrated in six `*Shelf.kt` files | 12,182 |
| Types named `*UiModel` in the repo at baseline | 0 |

| File | Lines |
|---|---|
| `feature/book_detail/.../screen/BookDetailShelf.kt` | 2,864 |
| `feature/profile/.../screen/ProfileShelf.kt` | 2,368 |
| `feature/library/.../screen/LibraryShelf.kt` | 2,050 |
| `feature/explore/.../screen/ExploreShelf.kt` | 1,853 |
| `feature/reading/.../screen/ReadingShelf.kt` | 1,620 |
| `feature/settings/.../screen/SettingsShelf.kt` | 1,427 |

## Gates

Convention does not hold a boundary — `:core:designsystem` is the proof. Every rule below is a build
failure.

- [x] **G1 — `:core:component` ban list.** `checkModuleGraph`'s `componentLibraryAllowedProjects` /
      `componentLibraryBannedGroups` / `conventionProvidedCoordinates`, paired with a scoped detekt
      `ForbiddenImport` rule (`component-contract.md` § 7.4) that catches usage the coordinate check
      cannot see.
- [x] **G2 — `:core:designsystem` has zero project dependencies.** `zeroProjectDependencyModules` in
      `checkModuleGraph`, paired with a detekt `ForbiddenImport` on `core.domain.**` scoped to
      `**/core/designsystem/**`.
- [x] **G3 — The `:core:designsystem` → `:core:book` api allowlist row is gone.**
- [ ] **G4 — Composable budget ratchet.** A `checkComponentBudget` task counting `@Composable`
      declarations outside `:core:component` (excluding `*Preview` functions and platform
      `expect`/`actual`s). Ceiling set to the post-migration measured value; a rise fails the build.
      Wire into `check`. `steps/s12-close-out.md` S12-1 writes it.
- [ ] **G5 — Doc rule.** `docs/reference/design-system/` updated in the same change as any
      design-system change — enforced by `softcover-reviewer`, which treats a design-system change
      with no doc update as a blocker.
- [ ] **G6 — `./gradlew check` green**, including `styleCheck` (type-resolved detekt across every
      module) and `ktlintCheck`.

**Open:** `:app:projectHealth` is unreachable on this machine (the pre-existing `JdkImageTransform`
failure); CI on a working toolchain covers it. `checkComponentBudget` (G4) is not written yet — the
count is still falling.

## Out of scope

Not components; they stay where they are.

- Screen composition roots — `*Screen`, `*ScreenLayout`, `*Shelf`, `Content`, `*Overlays`. S10 shrinks
  them; it does not move them.
- Navigation shells — `orchestration/presentation/`: `App`, `DesktopApp`, `CompactNavShell`,
  `WideNavShell`, `TabRootHost`, `BottomFloatingBar`, `DockedBottomNavigationBar`, `NavigationRailBar`,
  `EditorialSidebar`, `SidebarItem`, `BookDetailPaneHost`, `ReAuthDialog`.
- Platform `expect`/`actual` — `BarcodeScanner` (common/android/ios/jvm), `isCameraAvailable`,
  `isCameraPermissionGranted`, `rememberCameraPermissionRequester`, `Theme.{android,ios,jvm}`,
  `TransientNavArg.{android,jvm}`.
- Debug screens — `MotionDebugScreen`, `ShareCardDebugScreen`, `DebugRoutesSection`, in
  `app/src/debug/`. `DebugRoutesContent`, the seam that binds them per build type, is in
  `:core:presentation`.
- Share cards keep their own family — never folded into `BookCard` — but migrated in S4 with
  everything else in `:core:designsystem` (§ 7.0's history, closed).
- `@Preview` functions — excluded from the G4 budget count.

## Risks

Cross-cutting only; S7's own risks live in `steps/s7-bookcard.md`.

| Risk | Where | Mitigation |
|---|---|---|
| Long-lived red branch — all-at-once means the module split's compile breakage is resolved inside the branch. | S3, S4 | Stage boundaries must compile. Commit per stage so the PR is reviewable commit-by-commit even though it merges once. |
| Session loss mid-migration | any | This directory. Update checkboxes in the same commit as the work; keep the branch name in the header. |
| New component docs regrow `components.md` | any | The file is an index; new component docs go into KDoc on the UI model plus one index line. |
| Reviewer load — a single PR of this size is not reviewable in the normal way. | merge | Commit-per-stage discipline; run `softcover-reviewer` per stage, not once at the end. |
